package com.air.nc5dev.util.subscribe;

import com.air.nc5dev.util.subscribe.itf.ISubscriber;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 发布-订阅 消费工具类 </br>
 * A 发布一个 主题消息，B 被动接收下消息 来消费A发布的</br>
 * </br>
 * </br>
 *
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5+, V1.0
 * @date 2020/2/4 0004 15:03
 * @project
 */
@Deprecated
public final class PubSubUtil {
    /**
     * 消息订阅者，key 消息主题key， value 订阅者集合
     **/
    private final ConcurrentHashMap<String, List<ISubscriber>> subscribers = new ConcurrentHashMap<>();
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 100
            , 1, TimeUnit.MILLISECONDS
            , new ArrayBlockingQueue<>(10)
            , new ThreadFactory() {
        private AtomicInteger num = new AtomicInteger(0);

        @Override
        public Thread newThread(@NotNull Runnable r) {
            return new Thread(r, "thread-PubSubUtil-subscriber-msg-accept-"
                    + num.incrementAndGet());
        }
    }
    );

    private static final Object lock = new Object();
    private static final PubSubUtil INSTANCE = new PubSubUtil();

    /**
     * 订阅某个主题消息       </br>
     * </br>
     * </br>
     * </br>
     *
     * @return boolean true 订阅成功
     * @author air Email: 209308343@qq.com
     * @date 2020/2/4 0004 15:14
     * @Param [key, sub] 主题key，订阅者
     */
    public static final boolean subscribe(@NotNull final String key, ISubscriber sub) {
        /*PubSubUtil me = getInstance();
        List<ISubscriber> subscriberArrayList = me.subscribers.get(key);
        if (subscriberArrayList == null) {
            synchronized (lock){
                subscriberArrayList = new CopyOnWriteArrayList<>();
                me.subscribers.put(key, subscriberArrayList);
            }
        }

        synchronized (subscriberArrayList){ //莫得差别 key.intern()
            return subscriberArrayList.add(sub);
        }*/
        return true;
    }

    /**
     * 同步：发布一个主题消息       </br>
     * </br>
     * </br>
     * </br>
     *
     * @author air Email: 209308343@qq.com
     * @date 2020/2/4 0004 15:14
     * @Param [key, sub] 主题key，消息
     */
    public static final void publish(@NotNull final String key, Object msg) {
       /* PubSubUtil me = getInstance();
        List<ISubscriber> subscriberCopyOnWriteArrayList = me.subscribers.get(key);
        if (subscriberCopyOnWriteArrayList == null) {
            return ;
        }
        subscriberCopyOnWriteArrayList.stream().forEach(sub -> {
            try {
                sub.accept(msg);
            } catch (Exception e) {
                LogUtil.error(e.toString(), e);
            }
        });*/
    }

    /**
     * 异步: 发布一个主题消息       </br>
     * </br>
     * </br>
     * </br>
     *
     * @author air Email: 209308343@qq.com
     * @date 2020/2/4 0004 15:14
     * @Param [key, sub] 主题key，消息
     */
    public static final void publishAsync(@NotNull final String key, Object msg) {
       /* ThreadPoolExecutor threadPoolExecutor = getInstance().getThreadPoolExecutor();
        threadPoolExecutor.submit(() -> {
            publish(key, msg);
        });*/
    }

    /**
     * 获得 实例对象         </br>
     * </br>
     * </br>
     * </br>
     *
     * @return com.air.nc5dev.util.subscribe.PubSubUtil
     * @author air Email: 209308343@qq.com
     * @date 2020/2/4 0004 15:09
     * @Param []
     */
    public static final PubSubUtil getInstance() {
        return INSTANCE;
    }

    public ConcurrentHashMap<String, List<ISubscriber>> getSubscribers() {
        return subscribers;
    }

    public ThreadPoolExecutor getThreadPoolExecutor() {
        return threadPoolExecutor;
    }

    private PubSubUtil() {
    }
}
