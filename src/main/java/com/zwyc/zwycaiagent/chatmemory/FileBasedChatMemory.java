package com.zwyc.zwycaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.messages.Message;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileBasedChatMemory implements ChatMemoryRepository {

    private final String BASE_DIR;

    private static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    // 构造对象时，指定文件保存目录
    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
    }

    @Override
    public void saveAll(String conversationId, List<Message> messages) {
        // 对应原来的 add(conversationId, List)
        List<Message> messageList = getOrCreateConversation(conversationId);
        messageList.addAll(messages);
        saveConversation(conversationId, messageList);
    }


    @Override
    public List<String> findConversationIds() {
        return List.of();
    }

    @Override
    public List<Message> findByConversationId(String conversationId) {
        // 对应原来的 getOrCreateConversation，返回全部
        return getOrCreateConversation(conversationId);
    }

    @Override
    public void deleteByConversationId(String conversationId) {
        // 对应原来的 clear
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    private void saveConversation(String conversationId, List<Message> messages) {
        File file = new File(BASE_DIR + conversationId);
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取或创建会话消息的列表
     *
     * @param conversationId
     * @return
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * 每个会话文件单独保存
     *
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR + conversationId + ".kryo");
    }
}
