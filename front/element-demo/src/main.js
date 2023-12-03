import Vue from 'vue'
import ElementUI from 'element-ui'
import 'element-ui/lib/theme-chalk/index.css'
import 'font-awesome/css/font-awesome.min.css';

import DataDictionary from './components/DataDictionary.vue';

Vue.config.productionTip = false
Vue.prototype.$window = window

Vue.use(ElementUI)

// 注册一个全局自定义指令 `v-focus`
Vue.directive('drag', {
        inserted: function (el) {
            const dragDom = el;
            dragDom.style.cursor = "e-resize";
            dragDom.onmousedown = (e) => {
                // 鼠标按下，计算当前元素距离可视区的距离
                const disX = e.clientX
                const w = dragDom.clientWidth
                const minW = 240
                const maxW = 600
                var nw
                document.onmousemove = function (e) {
                    // 通过事件委托，计算移动的距离
                    const l = e.clientX - disX
                    // 改变当前元素宽度，不可超过最小最大值
                    nw = w + l
                    nw = nw < minW ? minW : nw
                    nw = nw > maxW ? maxW : nw
                    dragDom.style.width = `${nw}px`
                }

                document.onmouseup = function (e) {
                    document.onmousemove = null
                    document.onmouseup = null
                }
            }
        }
    }
)

new Vue({
    el: '#app',
    render: h => h(DataDictionary)
});


