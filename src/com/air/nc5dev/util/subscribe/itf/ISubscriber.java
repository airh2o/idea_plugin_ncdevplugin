package com.air.nc5dev.util.subscribe.itf;

/**
 * 订阅者 接口</br>
 * </br>
 * </br>
 * </br>
 * @see com.air.nc5dev.util.subscribe.PubSubUtil
 * @author air Email: 209308343@qq.com
 * @version NC505, JDK1.5
 * @date 2020/2/4 0004 15:07
 * @project
 */
public interface ISubscriber<T> {
    /**
      *    接受消息       </br>
      *           </br>
      *           </br>
      *           </br>
      * @author air Email: 209308343@qq.com
      * @date 2020/2/4 0004 15:17
      * @Param []
      * @return void
     */
    void accept(T msg);
}
