package com.air.nc5dev.ui.actionurlsearch;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class AutoCompleteComponet {
    public static class StationConstant {
        /**
         * 此处站点分层2个字符串 因为超过了字符串的存储长度(65535) 中国的火车站点还真是多
         */
        // 所有站点常量类
        public final static String stationString1 = "@bjb|北京北|VAP|beijingbei|bjb|0@bjd|北京东|BOP|beijingdong|bjd|1@bji" +
                "|北京|BJP|beijing|bj|2@bjn|北京南|VNP|beijingnan|bjn|3@bjx|北京西|BXP|beijingxi|bjx|4@cqb|重庆北|CUW" +
                "|chongqingbei|zqb|5@cqi|重庆|CQW|chongqing|zq|6@cqn|重庆南|CRW|chongqingnan|zqn|7@sha|上海|SHH|shanghai|sh" +
                "|8@shn|上海南|SNH|shanghainan|shn|9@shq|上海虹桥|AOH|shanghaihongqiao|shhq|10@shx|上海西|SXH|shanghaixi|shx|11" +
                "@tjb|天津北|TBP|tianjinbei|tjb|12@tji|天津|TJP|tianjin|tj|13@tjn|天津南|TIP|tianjinnan|tjn|14@tjx|天津西|TXP" +
                "|tianjinxi|tjx|15@cch|长春|CCT|changchun|cc|16@ccn|长春南|CET|changchunnan|ccn|17@ccx|长春西|CRT|changchunxi" +
                "|ccx|18@cdd|成都东|ICW|chengdudong|cdd|19@cdn|成都南|CNW|chengdunan|cdn|20@cdu|成都|CDW|chengdu|cd|21@csh" +
                "|长沙|CSQ|changsha|cs|22@csn|长沙南|CWQ|changshanan|csn|23@fzh|福州|FZS|fuzhou|fz|24@fzn|福州南|FYS|fuzhounan" +
                "|fzn|25@gya|贵阳|GIW|guiyang|gy|26@gzb|广州北|GBQ|guangzhoubei|gzb|27@gzd|广州东|GGQ|guangzhoudong|gzd|28" +
                "@gzh|广州|GZQ|guangzhou|gz|29@gzn|广州南|IZQ|guangzhounan|gzn|30@heb|哈尔滨|HBB|haerbin|heb|31@hed|哈尔滨东|VBB" +
                "|harbindong|hebd|32@hex|哈尔滨西|VAB|haerbinxi|hebx|33@hfe|合肥|HFH|hefei|hf|34@hfx|合肥西|HTH|hefeixi|hfx|35" +
                "@hhd|呼和浩特东|NDC|huhehaotedong|hhhtd|36@hht|呼和浩特|HHC|hohhot|hhht|37@hkd|海口东|HMQ|haikoudong|hkd|38@hko" +
                "|海口|VUQ|haikou|hk|39@hzh|杭州|HZH|hangzhou|hz|40@hzn|杭州南|XHH|hangzhounan|hzn|41@jna|济南|JNK|jinan|jn|42" +
                "@jnd|济南东|JAK|jinandong|jnd|43@jnx|济南西|JGK|jinanxi|jnx|44@kmi|昆明|KMM|kunming|km|45@kmx|昆明西|KXM" +
                "|kunmingxi|kmx|46@lsa|拉萨|LSO|lasa|ls|47@lzd|兰州东|LVJ|lanzhoudong|lzd|48@lzh|兰州|LZJ|lanzhou|lz|49@lzx" +
                "|兰州西|LAJ|lanzhouxi|lzx|50@nch|南昌|NCG|nanchang|nc|51@nji|南京|NJH|nanjing|nj|52@njn|南京南|NKH|nanjingnan" +
                "|njn|53@njx|南京西|NIH|nanjingxi|njx|54@nni|南宁|NNZ|nanning|nn|55@sjb|石家庄北|VVP|shijiazhuangbei|sjzb|56" +
                "@sjz|石家庄|SJP|shijiazhuang|sjz|57@sya|沈阳|SYT|shenyang|sy|58@syb|沈阳北|SBT|shenyangbei|syb|59@syd" +
                "|沈阳东|SDT|shenyangdong|syd|60@tyb|太原北|TBV|taiyuanbei|tyb|61@tyd|太原东|TDV|taiyuandong|tyd|62@tyu|太原|TYV" +
                "|taiyuan|ty|63@wha|武汉|WHN|wuhan|wh|64@wjx|王家营西|KNM|wangjiayingxi|wjyx|65@wlq|乌鲁木齐|WMR|wulumuqi|wlmq" +
                "|66@xab|西安北|EAY|xianbei|xab|67@xan|西安|XAY|xian|xa|68@xan|西安南|CAY|xiannan|xan|69@xnx|西宁西|XXO|xiningxi" +
                "|xnx|70@ych|银川|YIJ|yinchuan|yc|71@ycn|银川南|YEJ|yinchuannan|ycn|72@zzh|郑州|ZZF|zhengzhou|zz|73@aes" +
                "|阿尔山|ART|aershan|aes|74@aka|安康|AKY|ankang|ak|75@aks|阿克苏|ASR|akesu|aks|76@alh|阿里河|AHX|alihe|alh|77" +
                "@alk|阿拉山口|AKR|alashankou|alsk|78@api|安平|APT|anping|ap|79@aqi|安庆|AQH|anqing|aq|80@ash|安顺|ASW|anshun" +
                "|as|81@ash|鞍山|AST|anshan|as|82@aya|安阳|AYF|anyang|ay|83@ban|北安|BAB|beian|ba|84@bbu|蚌埠|BBH|bengbu|bb" +
                "|85@bch|白城|BCT|baicheng|bc|86@bha|北海|BHZ|beihai|bh|87@bhe|白河|BEL|baihe|bh|88@bji|白涧|BAP|baijian|bj" +
                "|89@bji|宝鸡|BJY|baoji|bj|90@bji|滨江|BJB|binjiang|bj|91@bkt|博克图|BKX|bugt|bkt|92@bse|百色|BIZ|baise|bs|93" +
                "@bss|白山市|HJL|baishanshi|bss|94@bta|北台|BTT|beitai|bt|95@btd|包头东|BDC|baotoudong|btd|96@bto|包头|BTC" +
                "|baotou|bt|97@bts|北屯市|BXR|beitunshi|bts|98@bxi|本溪|BXT|benxi|bx|99@byb|白云鄂博|BEC|bayanobo|byeb|100@byx" +
                "|白银西|BXJ|baiyinxi|byx|101@bzh|亳州|BZH|bozhou|bz|102@cbi|赤壁|CBN|chibi|cb|103@cde|常德|VGQ|changde|cd|104" +
                "@cde|承德|CDP|chengde|cd|105@cdi|长甸|CDT|changdian|cd|106@cfe|赤峰|CFD|chifeng|cf|107@cli|茶陵|CDG|chaling" +
                "|cl|108@cna|苍南|CEH|cangnan|cn|109@cpi|昌平|CPP|changping|cp|110@cre|崇仁|CRG|chongren|cr|111@ctu|昌图|CTT" +
                "|changtu|ct|112@ctz|长汀镇|CDB|changtingzhen|ctz|113@cxi|崇信|CIJ|chongxin|cx|114@cxi|曹县|CXK|caoxian|cx" +
                "|115@cxi|楚雄|COM|chuxiong|cx|116@cxt|陈相屯|CXT|chenxiangtun|cxt|117@czb|长治北|CBF|changzhibei|czb|118@czh" +
                "|长征|CZJ|changzheng|cz|119@czh|池州|IYH|chizhou|cz|120@czh|常州|CZH|changzhou|cz|121@czh|郴州|CZQ|chenzhou" +
                "|cz|122@czh|长治|CZF|changzhi|cz|123@czh|沧州|COP|cangzhou|cz|124@czu|崇左|CZZ|chongzuo|cz|125@dab|大安北|RNT" +
                "|daanbei|dab|126@dch|大成|DCT|dacheng|dc|127@ddo|丹东|DUT|dandong|dd|128@dfh|东方红|DFB|dongfanghong|dfh" +
                "|129@dgd|东莞东|DMQ|dongguandong|dwd|130@dhs|大虎山|DHD|dahushan|dhs|131@dhu|敦煌|DHJ|dunhuang|dh|132@dhu" +
                "|敦化|DHL|dunhua|dh|133@dhu|德惠|DHT|dehui|dh|134@djc|东京城|DJB|dongjingcheng|djc|135@dji|大涧|DFP|dajian|dj" +
                "|136@djy|都江堰|DDW|dujiangyan|djy|137@dlb|大连北|DFT|dalianbei|dlb|138@dli|大理|DKM|dali|dl|139@dli|大连|DLT" +
                "|dalian|dl|140@dna|定南|DNG|dingnan|dn|141@dqi|大庆|DZX|daqing|dq|142@dsh|东胜|DOC|dongsheng|ds|143@dsq" +
                "|大石桥|DQT|dashiqiao|dsq|144@dto|大同|DTV|datong|dt|145@dyi|东营|DPK|dongying|dy|146@dys|大杨树|DUX|dayangshu" +
                "|dys|147@dyu|都匀|RYW|duyun|dy|148@dzh|邓州|DOF|dengzhou|dz|149@dzh|达州|RXW|dazhou|dz|150@dzh|德州|DZP" +
                "|dezhou|dz|151@ejn|额济纳|EJC|ejina|ejn|152@eli|二连|RLC|erlian|el|153@esh|恩施|ESN|enshi|es|154@fcg" +
                "|防城港|FEZ|fangchenggang|fcg|155@fdi|福鼎|FES|fuding|fd|156@fld|风陵渡|FLV|fenglingdu|fld|157@fli|涪陵|FLW" +
                "|fuling|fl|158@flj|富拉尔基|FRX|fulaerji|flej|159@fsb|抚顺北|FET|fushunbei|fsb|160@fsh|佛山|FSQ|foshan|fs|161" +
                "@fxi|阜新|FXD|fuxin|fx|162@fya|阜阳|FYH|fuyang|fy|163@gem|格尔木|GRO|geermu|gem|164@gha|广汉|GHW|guanghan|gh" +
                "|165@gji|古交|GJV|gujiao|gj|166@glb|桂林北|GBZ|guilinbei|glb|167@gli|古莲|GRX|gulian|gl|168@gli|桂林|GLZ" +
                "|guilin|gl|169@gsh|固始|GXN|gushi|gs|170@gsh|广水|GSN|guangshui|gs|171@gta|干塘|GNJ|gantang|gt|172@gyu" +
                "|广元|GYW|guangyuan|gy|173@gzh|赣州|GZG|ganzhou|gz|174@gzl|公主岭|GLT|gongzhuling|gzl|175@gzn|公主岭南|GBT" +
                "|gongzhulingnan|gzln|176@han|淮安|AUH|huaian|ha|177@hbe|鹤北|HMB|hebei|hb|178@hbe|淮北|HRH|huaibei|hb|179" +
                "@hbi|淮滨|HVN|huaibin|hb|180@hbi|河边|HBV|hebian|hb|181@hch|潢川|KCN|huangchuan|hc|182@hch|韩城|HCY|hancheng" +
                "|hc|183@hda|邯郸|HDP|handan|hd|184@hdz|横道河子|HDB|hengdaohezi|hdhz|185@hga|鹤岗|HGB|hegang|hg|186@hgt" +
                "|皇姑屯|HTT|huanggutun|hgt|187@hgu|红果|HEM|hongguo|hg|188@hhe|黑河|HJB|heihe|hh|189@hhu|怀化|HHQ|huaihua|hh" +
                "|190@hko|汉口|HKN|hankou|hk|191@hld|葫芦岛|HLD|huludao|hld|192@hle|海拉尔|HRX|hailaer|hle|193@hll|霍林郭勒|HWD" +
                "|huolinguole|hlgl|194@hlu|海伦|HLB|hailun|hl|195@hma|侯马|HMV|houma|hm|196@hmi|哈密|HMR|hami|hm|197@hna" +
                "|淮南|HAH|huainan|hn|198@hna|桦南|HNB|huanan|hn|199@hnx|海宁西|EUH|hainingxi|hnx|200@hqi|鹤庆|HQM|heqing|hq" +
                "|201@hrb|怀柔北|HBP|huairoubei|hrb|202@hro|怀柔|HRP|huairou|hr|203@hsd|黄石东|OSN|huangshidong|hsd|204@hsh" +
                "|华山|HSY|huashan|hs|205@hsh|黄石|HSN|huangshi|hs|206@hsh|黄山|HKH|huangshan|hs|207@hsh|衡水|HSP|hengshui|hs" +
                "|208@hya|衡阳|HYQ|hengyang|hy|209@hze|菏泽|HIK|heze|hz|210@hzh|贺州|HXZ|hezhou|hz|211@hzh|汉中|HOY|hanzhong" +
                "|hz|212@hzh|惠州|HCQ|huizhou|hz|213@jan|吉安|VAG|jian|ja|214@jan|集安|JAL|jian|ja|215@jbc|江边村|JBG" +
                "|jiangbiancun|jbc|216@jch|晋城|JCF|jincheng|jc|217@jcj|金城江|JJZ|jinchengjiang|jcj|218@jdz|景德镇|JCG" +
                "|jingdezhen|jdz|219@jfe|嘉峰|JFF|jiafeng|jf|220@jgq|加格达奇|JGX|jagdaqi|jgdq|221@jgs|井冈山|JGG|jinggangshan" +
                "|jgs|222@jhe|蛟河|JHL|jiaohe|jh|223@jhn|金华南|RNH|jinhuanan|jhn|224@jhx|金华西|JBH|jinhuaxi|jhx|225@jji" +
                "|九江|JJG|jiujiang|jj|226@jli|吉林|JLL|jilin|jl|227@jme|荆门|JMN|jingmen|jm|228@jms|佳木斯|JMB|jiamusi|jms" +
                "|229@jni|济宁|JIK|jining|jn|230@jnn|集宁南|JAC|jiningnan|jnn|231@jqu|酒泉|JQJ|jiuquan|jq|232@jsh|江山|JUH" +
                "|jiangshan|js|233@jsh|吉首|JIQ|jishou|js|234@jta|九台|JTL|jiutai|jt|235@jts|镜铁山|JVJ|jingtieshan|jts|236" +
                "@jxi|鸡西|JXB|jixi|jx|237@jxi|蓟县|JKP|jixian|jx|238@jxx|绩溪县|JRH|jixixian|jxx|239@jyg|嘉峪关|JGJ|jiayuguan" +
                "|jyg|240@jyo|江油|JFW|jiangyou|jy|241@jzh|锦州|JZD|jinzhou|jz|242@jzh|金州|JZT|jinzhou|jz|243@kel|库尔勒|KLR" +
                "|kuerle|kel|244@kfe|开封|KFF|kaifeng|kf|245@kla|岢岚|KLV|kelan|kl|246@kli|凯里|KLW|kaili|kl|247@ksh|喀什|KSR" +
                "|kashi|ks|248@ksn|昆山南|KNH|kunshannan|ksn|249@ktu|奎屯|KTR|kuitun|kt|250@kyu|开原|KYT|kaiyuan|ky|251@lan" +
                "|六安|UAH|luan|la|252@lba|灵宝|LBF|lingbao|lb|253@lcg|芦潮港|UCH|luchaogang|lcg|254@lch|隆昌|LCW|longchang|lc" +
                "|255@lch|陆川|LKZ|luchuan|lc|256@lch|利川|LCN|lichuan|lc|257@lch|临川|LCG|linchuan|lc|258@lch|潞城|UTP" +
                "|lucheng|lc|259@lda|鹿道|LDL|ludao|ld|260@ldi|娄底|LDQ|loudi|ld|261@lfe|临汾|LFV|linfen|lf|262@lgz|良各庄|LGP" +
                "|lianggezhuang|lgz|263@lhe|临河|LHC|linhe|lh|264@lhe|漯河|LON|luohe|lh|265@lhu|绿化|LWJ|lvhua|lh|266@lhu" +
                "|隆化|UHP|longhua|lh|267@lji|丽江|LHM|lijiang|lj|268@lji|临江|LQL|linjiang|lj|269@lji|龙井|LJL|longjing|lj" +
                "|270@ljy|龙家营|LKP|longjiaying|ljy|271@lli|吕梁|LHV|lvliang|ll|272@lli|醴陵|LLG|liling|ll|273@lpi|滦平|UPP" +
                "|luanping|lp|274@lps|六盘水|UMW|liupanshui|lps|275@lqi|灵丘|LVV|lingqiu|lq|276@lsh|旅顺|LST|lvshun|ls|277" +
                "@lxi|陇西|LXJ|longxi|lx|278@lxi|澧县|LEQ|lixian|lx|279@lxi|兰溪|LWH|lanxi|lx|280@lxi|临西|UEP|linxi|lx|281" +
                "@lya|耒阳|LYQ|leiyang|ly|282@lya|洛阳|LYF|luoyang|ly|283@lya|龙岩|LYS|longyan|ly|284@lyd|洛阳东|LDF" +
                "|luoyangdong|lyd|285@lyd|连云港东|UKH|lianyungangdong|lygd|286@lyi|临沂|LVK|linyi|ly|287@lym|洛阳龙门|LLF" +
                "|luoyanglongmen|lylm|288@lyu|柳园|DHR|liuyuan|ly|289@lyu|凌源|LYD|lingyuan|ly|290@lyu|辽源|LYL|liaoyuan|ly" +
                "|291@lzh|立志|LZX|lizhi|lz|292@lzh|柳州|LZZ|liuzhou|lz|293@lzh|辽中|LZD|liaozhong|lz|294@mch|麻城|MCN" +
                "|macheng|mc|295@mdh|免渡河|MDX|mianduhe|mdh|296@mdj|牡丹江|MDB|mudanjiang|mdj|297@meg|莫尔道嘎|MRX|mordaga" +
                "|medg|298@mgu|满归|MHX|mangui|mg|299@mgu|明光|MGH|mingguang|mg|300@mhe|漠河|MVX|mohe|mh|301@mji|梅江|MKQ" +
                "|meijiang|mj|302@mmd|茂名东|MDQ|maomingdong|mmd|303@mmi|茂名|MMZ|maoming|mm|304@msh|密山|MSB|mishan|ms|305" +
                "@msj|马三家|MJT|masanjia|msj|306@mwe|麻尾|VAW|mawei|mw|307@mya|绵阳|MYW|mianyang|my|308@mzh|梅州|MOQ|meizhou" +
                "|mz|309@mzl|满洲里|MLX|manzhouli|mzl|310@nbd|宁波东|NVH|ningbodong|nbd|311@nch|南岔|NCB|nancha|nc|312@nch" +
                "|南充|NCW|nanchong|nc|313@nda|南丹|NDZ|nandan|nd|314@ndm|南大庙|NMP|nandamiao|ndm|315@nfe|南芬|NFT|nanfen|nf" +
                "|316@nhe|讷河|NHX|nehe|nh|317@nji|嫩江|NGX|nenjiang|nj|318@nji|内江|NJW|neijiang|nj|319@npi|南平|NPS|nanping" +
                "|np|320@nto|南通|NUH|nantong|nt|321@nya|南阳|NFF|nanyang|ny|322@nzs|碾子山|NZX|nianzishan|nzs|323@pds" +
                "|平顶山|PEN|pingdingshan|pds|324@pji|盘锦|PVD|panjin|pj|325@pli|平凉|PIJ|pingliang|pl|326@pln|平凉南|POJ" +
                "|pingliangnan|pln|327@pqu|平泉|PQP|pingquan|pq|328@psh|坪石|PSQ|pingshi|ps|329@pxi|萍乡|PXG|pingxiang|px" +
                "|330@pxi|凭祥|PXZ|pingxiang|px|331@pxx|郫县西|PCW|pixianxi|pxx|332@pzh|攀枝花|PRW|panzhihua|pzh|333@qch" +
                "|蕲春|QRN|qichun|qc|334@qcs|青城山|QSW|qingchengshan|qcs|335@qda|青岛|QDK|qingdao|qd|336@qhc|清河城|QYP" +
                "|qinghecheng|qhc|337@qji|黔江|QNW|qianjiang|qj|338@qji|曲靖|QJM|qujing|qj|339@qjz|前进镇|QEB|qianjinzhen" +
                "|qjz|340@qqe|齐齐哈尔|QHX|qiqihaer|qqhe|341@qth|七台河|QTB|qitaihe|qth|342@qxi|沁县|QVV|qinxian|qx|343@qzd" +
                "|泉州东|QRS|quanzhoudong|qzd|344@qzh|泉州|QYS|quanzhou|qz|345@qzh|衢州|QEH|quzhou|qz|346@ran|融安|RAZ|rongan" +
                "|ra|347@rjg|汝箕沟|RQJ|rujigou|rjg|348@rji|瑞金|RJG|ruijin|rj|349@rzh|日照|RZK|rizhao|rz|350@scp|双城堡|SCB" +
                "|shuangchengpu|scb|351@sfh|绥芬河|SFB|suifenhe|sfh|352@sgd|韶关东|SGQ|shaoguandong|sgd|353@shg|山海关|SHD" +
                "|shanhaiguan|shg|354@shu|绥化|SHB|suihua|sh|355@sjf|三间房|SFX|sanjianfang|sjf|356@sjt|苏家屯|SXT|sujiatun" +
                "|sjt|357@sla|舒兰|SLL|shulan|sl|358@smi|三明|SMS|sanming|sm|359@smu|神木|OMY|shenmu|sm|360@smx|三门峡|SMF" +
                "|sanmenxia|smx|361@sna|商南|ONY|shangnan|sn|362@sni|遂宁|NIW|suining|sn|363@spi|四平|SPT|siping|sp|364@sqi" +
                "|商丘|SQF|shangqiu|sq|365@sra|上饶|SRG|shangrao|sr|366@ssh|韶山|SSQ|shaoshan|ss|367@sso|宿松|OAH|susong|ss" +
                "|368@sto|汕头|OTQ|shantou|st|369@swu|邵武|SWS|shaowu|sw|370@sxi|涉县|OEP|shexian|sx|371@sya|三亚|SEQ|sanya" +
                "|sy|372@sya|邵阳|SYQ|shaoyang|sy|373@sya|十堰|SNN|shiyan|sy|374@sys|双鸭山|SSB|shuangyashan|sys|375@syu" +
                "|松原|VYT|songyuan|sy|376@szh|深圳|SZQ|shenzhen|sz|377@szh|苏州|SZH|suzhou|sz|378@szh|随州|SZN|suizhou|sz" +
                "|379@szh|宿州|OXH|suzhou|sz|380@szh|朔州|SUV|shuozhou|sz|381@szx|深圳西|OSQ|shenzhenxi|szx|382@tba|塘豹|TBQ" +
                "|tangbao|tb|383@teq|塔尔气|TVX|tarqi|teq|384@tgu|潼关|TGY|tongguan|tg|385@tgu|塘沽|TGP|tanggu|tg|386@the" +
                "|塔河|TXX|tahe|th|387@thu|通化|THL|tonghua|th|388@tla|泰来|TLX|tailai|tl|389@tlf|吐鲁番|TFR|tulufan|tlf|390" +
                "@tli|通辽|TLD|tongliao|tl|391@tli|铁岭|TLT|tieling|tl|392@tlz|陶赖昭|TPT|taolaizhao|tlz|393@tme|图们|TML" +
                "|tumen|tm|394@tre|铜仁|RDQ|tongren|tr|395@tsb|唐山北|FUP|tangshanbei|tsb|396@tsf|田师府|TFT|tianshifu|tsf" +
                "|397@tsh|泰山|TAK|taishan|ts|398@tsh|天水|TSJ|tianshui|ts|399@tsh|唐山|TSP|tangshan|ts|400@typ|通远堡|TYT" +
                "|tongyuanpu|tyb|401@tys|太阳升|TQT|taiyangsheng|tys|402@tzh|泰州|UTH|taizhou|tz|403@tzi|桐梓|TZW|tongzi|tz" +
                "|404@tzx|通州西|TAP|tongzhouxi|tzx|405@wch|五常|WCB|wuchang|wc|406@wch|武昌|WCN|wuchang|wc|407@wfd|瓦房店|WDT" +
                "|wafangdian|wfd|408@whi|威海|WKK|weihai|wh|409@whu|芜湖|WHH|wuhu|wh|410@whx|乌海西|WXC|wuhaixi|whx|411@wjt" +
                "|吴家屯|WJT|wujiatun|wjt|412@wlo|武隆|WLW|wulong|wl|413@wlt|乌兰浩特|WWT|ulanhot|wlht|414@wna|渭南|WNY|weinan" +
                "|wn|415@wsh|威舍|WSM|weishe|ws|416@wts|歪头山|WIT|waitoushan|wts|417@wwe|武威|WUJ|wuwei|ww|418@wwn|武威南|WWJ" +
                "|wuweinan|wwn|419@wxi|无锡|WXH|wuxi|wx|420@wxi|乌西|WXR|wuxi|wx|421@wyl|乌伊岭|WPB|wuyiling|wyl|422@wys" +
                "|武夷山|WAS|wuyishan|wys|423@wyu|万源|WYY|wanyuan|wy|424@wzh|万州|WYW|wanzhou|wz|425@wzh|梧州|WZZ|wuzhou|wz" +
                "|426@wzh|温州|RZH|wenzhou|wz|427@wzn|温州南|VRH|wenzhounan|wzn|428@xch|西昌|ECW|xichang|xc|429@xch|许昌|XCF" +
                "|xuchang|xc|430@xcn|西昌南|ENW|xichangnan|xcn|431@xfa|香坊|XFB|xiangfang|xf|432@xga|轩岗|XGV|xuangang|xg" +
                "|433@xgu|兴国|EUG|xingguo|xg|434@xha|宣汉|XHY|xuanhan|xh|435@xhu|新会|EFQ|xinhui|xh|436@xhu|新晃|XLQ" +
                "|xinhuang|xh|437@xlt|锡林浩特|XTC|xilinhaote|xlht|438@xlx|兴隆县|EXP|xinglongxian|xlx|439@xmb|厦门北|XKS" +
                "|xiamenbei|xmb|440@xme|厦门|XMS|xiamen|xm|441@xmq|厦门高崎|XBS|xiamengaoqi|xmgq|442@xsh|秀山|ETW|xiushan|xs" +
                "|443@xsh|小市|XST|xiaoshi|xs|444@xta|向塘|XTG|xiangtang|xt|445@xwe|宣威|XWM|xuanwei|xw|446@xxi|新乡|XXF" +
                "|xinxiang|xx|447@xya|信阳|XUN|xinyang|xy|448@xya|咸阳|XYY|xianyang|xy|449@xya|襄阳|XFN|xiangyang|xy|450" +
                "@xyc|熊岳城|XYT|xiongyuecheng|xyc|451@xyi|兴义|XRZ|xingyi|xy|452@xyi|新沂|VIH|xinyi|xy|453@xyu|新余|XUG|xinyu" +
                "|xy|454@xzh|徐州|XCH|xuzhou|xz|455@yan|延安|YWY|yanan|ya|456@ybi|宜宾|YBW|yibin|yb|457@ybn|亚布力南|YWB" +
                "|yabulinan|ybln|458@ybs|叶柏寿|YBD|yebaishou|ybs|459@ycd|宜昌东|HAN|yichangdong|ycd|460@ych|永川|YCW" +
                "|yongchuan|yc|461@ych|宜春|YCG|yichun|yc|462@ych|宜昌|YCN|yichang|yc|463@ych|盐城|AFH|yancheng|yc|464@ych" +
                "|运城|YNV|yuncheng|yc|465@ych|伊春|YCB|yichun|yc|466@yci|榆次|YCV|yuci|yc|467@ycu|杨村|YBP|yangcun|yc|468" +
                "@yga|燕岗|YGW|yangang|yg|469@yji|永济|YIV|yongji|yj|470@yji|延吉|YJL|yanji|yj|471@yko|营口|YKT|yingkou|yk" +
                "|472@yks|牙克石|YKX|yakeshi|yks|473@yli|阎良|YNY|yanliang|yl|474@yli|玉林|YLZ|yulin|yl|475@yli|榆林|ALY|yulin" +
                "|yl|476@ymp|一面坡|YPB|yimianpo|ymp|477@yni|伊宁|YMR|yining|yn|478@ypg|阳平关|YAY|yangpingguan|ypg|479@ypi" +
                "|玉屏|YZW|yuping|yp|480@ypi|原平|YPV|yuanping|yp|481@yqi|延庆|YNP|yanqing|yq|482@yqq|阳泉曲|YYV|yangquanqu" +
                "|yqq|483@yqu|玉泉|YQB|yuquan|yq|484@yqu|阳泉|AQP|yangquan|yq|485@ysh|玉山|YNG|yushan|ys|486@ysh|营山|NUW" +
                "|yingshan|ys|487@ysh|燕山|AOP|yanshan|ys|488@ysh|榆树|YRT|yushu|ys|489@yta|鹰潭|YTG|yingtan|yt|490@yta" +
                "|烟台|YAK|yantai|yt|491@yth|伊图里河|YEX|yitulihe|ytlh|492@ytx|玉田县|ATP|yutianxian|ytx|493@ywu|义乌|YWH|yiwu" +
                "|yw|494@yxi|阳新|YON|yangxin|yx|495@yxi|义县|YXD|yixian|yx|496@yya|益阳|AEQ|yiyang|yy|497@yya|岳阳|YYQ" +
                "|yueyang|yy|498@yzh|永州|AOQ|yongzhou|yz|499@yzh|扬州|YLH|yangzhou|yz|500@zbo|淄博|ZBK|zibo|zb|501@zcd" +
                "|镇城底|ZDV|zhenchengdi|zcd|502@zgo|自贡|ZGW|zigong|zg|503@zhb|珠海北|ZIQ|zhuhaibei|zhb|504@zji|湛江|ZJZ" +
                "|zhanjiang|zj|505@zji|镇江|ZJH|zhenjiang|zj|506@zjj|张家界|DIQ|zhangjiajie|zjj|507@zjk|张家口|ZKP" +
                "|zhangjiakou|zjk|508@zjn|张家口南|ZMP|zhangjiakounan|zjkn|509@zko|周口|ZKN|zhoukou|zk|510@zlm|哲里木|ZLC" +
                "|zhelimu|zlm|511@zlt|扎兰屯|ZTX|zhalantun|zlt|512@zmd|驻马店|ZDN|zhumadian|zmd|513@zqi|肇庆|ZVQ|zhaoqing|zq" +
                "|514@zsz|周水子|ZIT|zhoushuizi|zsz|515@zto|昭通|ZDW|zhaotong|zt|516@zwe|中卫|ZWJ|zhongwei|zw|517@zya|资阳|ZYW" +
                "|ziyang|zy|518@zyi|遵义|ZIW|zunyi|zy|519@zzh|枣庄|ZEK|zaozhuang|zz|520@zzh|资中|ZZW|zizhong|zz|521@zzh" +
                "|株洲|ZZQ|zhuzhou|zz|522@zzx|枣庄西|ZFK|zaozhuangxi|zzx|523@aax|昂昂溪|AAX|angangxi|aax|524@ach|阿城|ACB" +
                "|acheng|ac|525@ada|安达|ADX|anda|ad|526@adi|安定|ADP|anding|ad|527@agu|安广|AGT|anguang|ag|528@ahe|艾河|AHP" +
                "|aihe|ah|529@ahu|安化|PKQ|anhua|ah|530@ajc|艾家村|AJJ|aijiacun|ajc|531@aji|鳌江|ARH|aojiang|aj|532@aji" +
                "|安家|AJB|anjia|aj|533@aji|阿金|AJD|ajin|aj|534@akt|阿克陶|AER|aketao|akt|535@aky|安口窑|AYY|ankouyao|aky|536" +
                "@alg|敖力布告|ALD|aolibugao|albg|537@alo|安龙|AUZ|anlong|al|538@als|阿龙山|ASX|alongshan|als|539@alu|安陆|ALN" +
                "|anlu|al|540@ame|阿木尔|JTX|amuer|ame|541@anz|阿南庄|AZM|ananzhuang|anz|542@aqx|安庆西|APH|anqingxi|aqx|543" +
                "@asx|鞍山西|AXT|anshanxi|asx|544@ata|安塘|ATV|antang|at|545@atb|安亭北|ASH|antingbei|atb|546@ats|阿图什|ATR" +
                "|atushi|ats|547@atu|安图|ATL|antu|at|548@axi|安溪|AXS|anxi|ax|549@bao|博鳌|BWQ|boao|ba|550@bbg|白壁关|BGV" +
                "|baibiguan|bbg|551@bbn|蚌埠南|BMH|bengbunan|bbn|552@bch|巴楚|BCR|bachu|bc|553@bch|板城|BUP|bancheng|bc|554" +
                "@bdh|北戴河|BEP|beidaihe|bdh|555@bdi|保定|BDP|baoding|bd|556@bdi|宝坻|BPP|baodi|bc|557@bdl|八达岭|ILP|badaling" +
                "|bdl|558@bdo|巴东|BNN|badong|bd|559@bgu|柏果|BGM|baiguo|bg|560@bha|布海|BUT|buhai|bh|561@bhd|白河东|BIY" +
                "|baihedong|bhd|562@bho|贲红|BVC|benhong|bh|563@bhs|宝华山|BWH|baohuashan|bhs|564@bhx|白河县|BEY|baihexian" +
                "|bhx|565@bjg|白芨沟|BJJ|baijigou|bjg|566@bjg|碧鸡关|BJM|bijiguan|bjg|567@bji|北滘|IBQ|beijiao|b|568@bji" +
                "|碧江|BLQ|bijiang|bj|569@bjp|白鸡坡|BBM|baijipo|bjp|570@bjs|笔架山|BSB|bijiashan|bjs|571@bjt|八角台|BTD" +
                "|bajiaotai|bjt|572@bka|保康|BKD|baokang|bk|573@bkp|白奎堡|BKB|baikuipu|bkb|574@bla|白狼|BAT|bailang|bl|575" +
                "@bla|百浪|BRZ|bailang|bl|576@ble|博乐|BOR|bole|bl|577@blg|宝拉格|BQC|baolage|blg|578@bli|巴林|BLX|balin|bl" +
                "|579@bli|宝林|BNB|baolin|bl|580@bli|北流|BOZ|beiliu|bl|581@bli|勃利|BLB|boli|bl|582@blk|布列开|BLR|buliekai" +
                "|blk|583@bls|宝龙山|BND|baolongshan|bls|584@bmc|八面城|BMD|bamiancheng|bmc|585@bmq|班猫箐|BNM|banmaoqing|bmq" +
                "|586@bmt|八面通|BMB|bamiantong|bmt|587@bmz|木镇|BZW|muzhen|mz|588@bmz|北马圈子|BRP|beimajuanzi|bmqz|589@bpn" +
                "|北票南|RPD|beipiaonan|bpn|590@bqi|白旗|BQP|baiqi|bq|591@bql|宝泉岭|BQB|baoquanling|bql|592@bsh|白沙|BSW" +
                "|baisha|bs|593@bsh|巴山|BAY|bashan|bs|594@bsj|白水江|BSY|baishuijiang|bsj|595@bsp|白沙坡|BPM|baishapo|bsp" +
                "|596@bss|白石山|BAL|baishishan|bss|597@bsz|白水镇|BUM|baishuizhen|bsz|598@bti|坂田|BTQ|bantian|bt|599@bto" +
                "|泊头|BZP|botou|bt|600@btu|北屯|BYP|beitun|bt|601@bxi|博兴|BXK|boxing|bx|602@bxt|八仙筒|VXD|baxiantong|bxt" +
                "|603@byg|白音察干|BYC|bayanqagan|bycg|604@byh|背荫河|BYB|beiyinhe|byh|605@byi|北营|BIV|beiying|by|606@byl" +
                "|巴彦高勒|BAC|bayangol|bygl|607@byl|白音他拉|BID|baiyintala|bytl|608@byq|鲅鱼圈|BYT|bayuquan|byq|609@bys" +
                "|白银市|BNJ|baiyinshi|bys|610@bys|白音胡硕|BCD|baiyinhushuo|byhs|611@bzh|巴中|IEW|bazhong|bz|612@bzh|霸州|RMP" +
                "|bazhou|bz|613@bzh|北宅|BVP|beizhai|bz|614@cbb|赤壁北|CIN|chibibei|cbb|615@cbg|查布嘎|CBC|chabuga|cbg|616" +
                "@cch|长城|CEJ|changcheng|cc|617@cch|长冲|CCM|changchong|cc|618@cdd|承德东|CCP|chengdedong|cdd|619@cfx" +
                "|赤峰西|CID|chifengxi|cfx|620@cga|嵯岗|CAX|cuogang|cg|621@cge|长葛|CEF|changge|cg|622@cgp|柴沟堡|CGV|chaigoupu" +
                "|cgb|623@cgu|城固|CGY|chenggu|cg|624@cgy|陈官营|CAJ|chenguanying|cgy|625@cgz|成高子|CZB|chenggaozi|cgz|626" +
                "@cha|草海|WBW|caohai|ch|627@che|柴河|CHB|chaihe|ch|628@che|册亨|CHZ|ceheng|ch|629@chk|草河口|CKT|caohekou|chk" +
                "|630@chk|崔黄口|CHP|cuihuangkou|chk|631@chu|巢湖|CIH|chaohu|ch|632@cjg|蔡家沟|CJT|caijiagou|cjg|633@cjh" +
                "|成吉思汗|CJX|qinggishan|cjsh|634@cji|岔江|CAM|chajiang|cj|635@cjp|蔡家坡|CJY|caijiapo|cjp|636@cko|沧口|CKK" +
                "|cangkou|ck|637@cle|昌乐|CLK|changle|cl|638@clg|超梁沟|CYP|chaolianggou|clg|639@cli|慈利|CUQ|cili|cl|640" +
                "@cli|昌黎|CLP|changli|cl|641@clz|长岭子|CLT|changlingzi|clz|642@cmi|晨明|CMB|chenming|cm|643@cno|长农|CNJ" +
                "|changnong|cn|644@cpb|昌平北|VBP|changpingbei|cpb|645@cpl|长坡岭|CPM|changpoling|cpl|646@cqi|辰清|CQB" +
                "|chenqing|cq|647@csh|楚山|CSB|chushan|cs|648@csh|长寿|EFW|changshou|cs|649@csh|蔡山|CON|caishan|cs|650@csh" +
                "|磁山|CSP|cishan|cs|651@csh|苍石|CST|cangshi|cs|652@csh|草市|CSL|caoshi|cs|653@csq|察素齐|CSC|chasuqi|csq|654" +
                "@cst|长山屯|CVT|changshantun|cst|655@cti|长汀|CES|changting|ct|656@ctx|昌图西|CPT|changtuxi|ctx|657@cwa" +
                "|春湾|CQQ|chunwan|cw|658@cxi|磁县|CIP|cixian|cx|659@cxi|岑溪|CNZ|cenxi|cx|660@cxi|辰溪|CXQ|chenxi|cx|661@cxi" +
                "|磁西|CRP|cixi|cx|662@cxn|长兴南|CFH|changxingnan|cxn|663@cya|磁窑|CYK|ciyao|cy|664@cya|朝阳|CYD|chaoyang|cy" +
                "|665@cya|春阳|CAL|chunyang|cy|666@cya|城阳|CEK|chengyang|cy|667@cyc|创业村|CEX|chuangyecun|cyc|668@cyc" +
                "|朝阳川|CYL|chaoyangchuan|cyc|669@cyd|朝阳地|CDD|chaoyangdi|cyd|670@cyu|长垣|CYF|changyuan|cy|671@cyz" +
                "|朝阳镇|CZL|chaoyangzhen|cyz|672@czb|滁州北|CUH|chuzhoubei|czb|673@czb|常州北|ESH|changzhoubei|czb|674@czh" +
                "|滁州|CXH|chuzhou|cz|675@czh|潮州|CKQ|chaozhou|cz|676@czh|常庄|CVK|changzhuang|cz|677@czl|曹子里|CFP|caozili" +
                "|czl|678@czw|车转湾|CWM|chezhuanwan|czw|679@czx|郴州西|ICQ|chenzhouxi|czx|680@czx|沧州西|CBP|cangzhouxi|czx" +
                "|681@dan|德安|DAG|dean|da|682@dan|东安|DAZ|dongan|da|683@dba|大坝|DBJ|daba|db|684@dba|大板|DBC|daban|db|685" +
                "@dba|大巴|DBD|daba|db|686@dba|到保|RBT|daobao|db|687@dbi|定边|DYJ|dingbian|db|688@dbj|东边井|DBB|dongbianjing" +
                "|dbj|689@dbs|德伯斯|RDT|debosi|dbs|690@dcg|打柴沟|DGJ|dachaigou|dcg|691@dch|德昌|DVW|dechang|dc|692@dda" +
                "|滴道|DDB|didao|dd|693@dde|大德|DEM|dade|dd|694@ddg|大磴沟|DKJ|dadenggou|ddg|695@ded|刀尔登|DRD|daoerdeng|ded" +
                "|696@dee|得耳布尔|DRX|derbur|debe|697@dfa|东方|UFQ|dongfang|df|698@dfe|丹凤|DGY|danfeng|df|699@dfe|东丰|DIL" +
                "|dongfeng|df|700@dgt|大官屯|DTT|daguantun|dgt|701@dgu|大关|RGW|daguan|dg|702@dgu|东光|DGP|dongguang|dg|703" +
                "@dgu|东莞|DAQ|dongguan|dw|704@dha|东海|DHB|donghai|dh|705@dhc|大灰厂|DHP|dahuichang|dhc|706@dhq|大红旗|DQD" +
                "|dahongqi|dhq|707@dhx|东海县|DQH|donghaixian|dhx|708@dhx|德惠西|DXT|dehuixi|dhx|709@djg|达家沟|DJT|dajiagou" +
                "|djg|710@dji|东津|DKB|dongjin|dj|711@dji|杜家|DJL|dujia|dj|712@djz|大旧庄|DJM|dajiuzhuang|djz|713@dkt" +
                "|大口屯|DKP|dakoutun|dkt|714@dla|东来|RVD|donglai|dl|715@dlh|德令哈|DHO|delingha|dlh|716@dlh|大陆号|DLC|daluhao" +
                "|dlh|717@dli|带岭|DLB|dailing|dl|718@dli|大林|DLD|dalin|dl|719@dlq|达拉特旗|DIC|dalateqi|dltq|720@dlt" +
                "|独立屯|DTX|dulitun|dlt|721@dlx|达拉特西|DNC|dalatexi|dltx|722@dmc|东明村|DMD|dongmingcun|dmc|723@dmh|洞庙河|DEP" +
                "|dongmiaohe|dmh|724@dmx|东明县|DNF|dongmingxian|dmx|725@dni|大拟|DNZ|dani|dn|726@dpf|大平房|DPD|dapingfang" +
                "|dpf|727@dps|大盘石|RPP|dapanshi|dps|728@dpu|大埔|DPI|dapu|dp|729@dpu|大堡|DVT|dapu|db|730@dqh|大其拉哈|DQX" +
                "|daqilaha|dqlh|731@dqi|道清|DML|daoqing|dq|732@dqs|对青山|DQB|duiqingshan|dqs|733@dqx|德清西|MOH|deqingxi" +
                "|dqx|734@dsh|东升|DRQ|dongsheng|ds|735@dsh|独山|RWW|dushan|ds|736@dsh|砀山|DKH|dangshan|ds|737@dsh|登沙河|DWT" +
                "|dengshahe|dsh|738@dsp|读书铺|DPM|dushupu|dsp|739@dst|大石头|DSL|dashitou|dst|740@dsz|大石寨|RZT|dashizhai" +
                "|dsz|741@dta|东台|DBH|dongtai|dt|742@dta|定陶|DQK|dingtao|dt|743@dta|灯塔|DGT|dengta|dt|744@dtb|大田边|DBM" +
                "|datianbian|dtb|745@dth|东通化|DTL|dongtonghua|dth|746@dtu|丹徒|RUH|dantu|dt|747@dtu|大屯|DNT|datun|dt|748" +
                "@dwa|东湾|DRJ|dongwan|dw|749@dwk|大武口|DFJ|dawukou|dwk|750@dwp|低窝铺|DWJ|diwopu|dwp|751@dwt|大王滩|DZZ" +
                "|dawangtan|dwt|752@dwz|大湾子|DFM|dawanzi|dwz|753@dxg|大兴沟|DXL|daxinggou|dxg|754@dxi|大兴|DXX|daxing|dx" +
                "|755@dxi|定西|DSJ|dingxi|dx|756@dxi|甸心|DXM|dianxin|dx|757@dxi|东乡|DXG|dongxiang|dx|758@dxi|代县|DKV" +
                "|daixian|dx|759@dxi|定襄|DXV|dingxiang|dx|760@dxu|东戌|RXP|dongxu|dx|761@dxz|东辛庄|DXD|dongxinzhuang|dxz" +
                "|762@dya|丹阳|DYH|danyang|dy|763@dya|大雁|DYX|dayan|dy|764@dya|德阳|DYW|deyang|dy|765@dya|当阳|DYN|dangyang" +
                "|dy|766@dyb|丹阳北|EXH|danyangbei|dyb|767@dyd|大英东|IAW|dayingdong|dyd|768@dyd|东淤地|DBV|dongyudi|dyd|769" +
                "@dyi|大营|DYV|daying|dy|770@dyu|定远|EWH|dingyuan|dy|771@dyu|岱岳|RYV|daiyue|dy|772@dyu|大元|DYZ|dayuan|dy" +
                "|773@dyz|大营镇|DJP|dayingzhen|dyz|774@dyz|大营子|DZD|dayingzi|dyz|775@dzc|大战场|DTJ|dazhanchang|dzc|776@dzd" +
                "|德州东|DIP|dezhoudong|dzd|777@dzh|低庄|DVQ|dizhuang|dz|778@dzh|东镇|DNV|dongzhen|dz|779@dzh|道州|DFZ|daozhou" +
                "|dz|780@dzh|东至|DCH|dongzhi|dz|781@dzh|兑镇|DWV|duizhen|dz|782@dzh|豆庄|ROP|douzhuang|dz|783@dzh|定州|DXP" +
                "|dingzhou|dz|784@dzy|大竹园|DZY|dazhuyuan|dzy|785@dzz|大杖子|DAP|dazhangzi|dzz|786@dzz|豆张庄|RZP" +
                "|douzhangzhuang|dzz|787@ebi|峨边|EBW|ebian|eb|788@edm|二道沟门|RDP|erdaogoumen|edgm|789@edw|二道湾|RDX" +
                "|erdaowan|edw|790@elo|二龙|RLD|erlong|el|791@elt|二龙山屯|ELA|erlongshantun|elst|792@eme|峨眉|EMW|emei|em" +
                "|793@eyi|二营|RYJ|erying|ey|794@ezh|鄂州|ECN|ezhou|ez|795@fan|福安|FAS|fuan|fa|796@fch|防城|FAZ|fangcheng|fc" +
                "|797@fch|丰城|FCG|fengcheng|fc|798@fcn|丰城南|FNG|fengchengnan|fcn|799@fdo|肥东|FIH|feidong|fd|800@fer" +
                "|发耳|FEM|faer|fe|801@fha|富海|FHX|fuhai|fh|802@fha|福海|FHR|fuhai|fh|803@fhc|凤凰城|FHT|fenghuangcheng|fhc" +
                "|804@fhu|奉化|FHH|fenghua|fh|805@fji|富锦|FIB|fujin|fj|806@fjt|范家屯|FTT|fanjiatun|fjt|807@flt|福利屯|FTB" +
                "|fulitun|flt|808@flz|丰乐镇|FZB|fenglezhen|flz|809@fna|阜南|FNH|funan|fn|810@fni|阜宁|AKH|funing|fn|811@fni" +
                "|抚宁|FNP|funing|fn|812@fqi|福清|FQS|fuqing|fq|813@fqu|福泉|VMW|fuquan|fq|814@fsc|丰水村|FSJ|fengshuicun|fsc" +
                "|815@fsh|丰顺|FUQ|fengshun|fs|816@fsh|繁峙|FSV|fanshi|fz|817@fsh|抚顺|FST|fushun|fs|818@fsk|福山口|FKP" +
                "|fushankou|fsk|819@fsu|扶绥|FSZ|fusui|fs|820@ftu|冯屯|FTX|fengtun|ft|821@fty|浮图峪|FYP|futuyu|fty|822@fxd" +
                "|富县东|FDY|fuxiandong|fxd|823@fxi|凤县|FXY|fengxian|fx|824@fxi|富县|FEY|fuxian|fx|825@fxi|费县|FXK|feixian" +
                "|fx|826@fya|凤阳|FUH|fengyang|fy|827@fya|汾阳|FAV|fenyang|fy|828@fyb|扶余北|FBT|fuyubei|fyb|829@fyi|分宜|FYG" +
                "|fenyi|fy|830@fyu|富源|FYM|fuyuan|fy|831@fyu|扶余|FYT|fuyu|fy|832@fyu|富裕|FYX|fuyu|fy|833@fzb|抚州北|FBG" +
                "|fuzhoubei|fzb|834@fzh|凤州|FZY|fengzhou|fz|835@fzh|丰镇|FZC|fengzhen|fz|836@fzh|范镇|VZK|fanzhen|fz|837" +
                "@gan|固安|GFP|guan|ga|838@gan|广安|VJW|guangan|ga|839@gbd|高碑店|GBP|gaobeidian|gbd|840@gbz|沟帮子|GBD" +
                "|goubangzi|gbz|841@gcd|甘草店|GDJ|gancaodian|gcd|842@gch|谷城|GCN|gucheng|gc|843@gch|藁城|GEP|gaocheng|gc" +
                "|844@gcz|古城镇|GZB|guchengzhen|gcz|845@gde|广德|GRH|guangde|gd|846@gdi|贵定|GTW|guiding|gd|847@gdn|贵定南|IDW" +
                "|guidingnan|gdn|848@gdo|古东|GDV|gudong|gd|849@gga|贵港|GGZ|guigang|gg|850@gga|官高|GVP|guangao|gg|851@ggm" +
                "|葛根庙|GGT|gegenmiao|ggm|852@ggu|甘谷|GGJ|gangu|gg|853@ggz|高各庄|GGP|gaogezhuang|ggz|854@ghe|甘河|GAX|ganhe" +
                "|gh|855@ghe|根河|GEX|genhe|gh|856@gjd|郭家店|GDT|guojiadian|gjd|857@gjz|孤家子|GKT|gujiazi|gjz|858@gla" +
                "|高老|GOB|gaolao|gl|859@gla|古浪|GLJ|gulang|gl|860@gla|皋兰|GEJ|gaolan|gl|861@glf|高楼房|GFM|gaoloufang|glf" +
                "|862@glh|归流河|GHT|guiliuhe|glh|863@gli|关林|GLF|guanlin|gl|864@glu|甘洛|VOW|ganluo|gl|865@glz|郭磊庄|GLP" +
                "|guoleizhuang|glz|866@gmi|高密|GMK|gaomi|gm|867@gmz|公庙子|GMC|gongmiaozi|gmz|868@gnh|工农湖|GRT|gongnonghu" +
                "|gnh|869@gns|广宁寺|GNT|guangningsi|gns|870@gnw|广南卫|GNM|guangnanwei|gnw|871@gpi|高平|GPF|gaoping|gp|872" +
                "@gqb|甘泉北|GEY|ganquanbei|gqb|873@gqc|共青城|GAG|gongqingcheng|gqc|874@gqk|甘旗卡|GQD|ganqika|gqk|875@gqu" +
                "|甘泉|GQY|ganquan|gq|876@gqz|高桥镇|GZD|gaoqiaozhen|gqz|877@gsh|赶水|GSW|ganshui|gs|878@gsh|灌水|GST|guanshui" +
                "|gs|879@gsk|孤山口|GSP|gushankou|gsk|880@gso|果松|GSL|guosong|gs|881@gsz|高山子|GSD|gaoshanzi|gsz|882@gsz" +
                "|嘎什甸子|GXD|gashidianzi|gsdz|883@gta|高台|GTJ|gaotai|gt|884@gta|高滩|GAY|gaotan|gt|885@gti|古田|GTS|gutian" +
                "|gt|886@gti|官厅|GTP|guanting|gt|887@gto|广通|GOM|guangtong|gt|888@gtx|官厅西|KEP|guantingxi|gtx|889@gxi" +
                "|贵溪|GXG|guixi|gx|890@gya|涡阳|GYH|guoyang|wy|891@gyi|巩义|GXF|gongyi|gy|892@gyi|高邑|GIP|gaoyi|gy|893@gyn" +
                "|巩义南|GYF|gongyinan|gyn|894@gyu|固原|GUJ|guyuan|gy|895@gyu|菇园|GYL|guyuan|gy|896@gyz|公营子|GYD|gongyingzi" +
                "|gyz|897@gze|光泽|GZS|guangze|gz|898@gzh|古镇|GNQ|guzhen|gz|899@gzh|瓜州|GZJ|guazhou|gz|900@gzh|高州|GSQ" +
                "|gaozhou|gz|901@gzh|固镇|GEH|guzhen|gz|902@gzh|盖州|GXT|gaizhou|gz|903@gzj|官字井|GOT|guanzijing|gzj|904" +
                "@gzp|革镇堡|GZT|gezhenpu|gzb|905@gzs|冠豸山|GSS|guanzhishan|gzs|906@gzx|盖州西|GAT|gaizhouxi|gzx|907@han" +
                "|红安|HWN|hongan|ha|908@han|淮安南|AMH|huaiannan|han|909@hax|红安西|VXN|honganxi|hax|910@hax|海安县|HIH" +
                "|haianxian|hax|911@hba|黄柏|HBL|huangbai|hb|912@hbe|海北|HEB|haibei|hb|913@hbi|鹤壁|HAF|hebi|hb|914@hch" +
                "|华城|VCQ|huacheng|hc|915@hch|合川|WKW|hechuan|hc|916@hch|河唇|HCZ|hechun|hc|917@hch|汉川|HCN|hanchuan|hc" +
                "|918@hch|海城|HCT|haicheng|hc|919@hct|黑冲滩|HCJ|heichongtan|hct|920@hcu|黄村|HCP|huangcun|hc|921@hcx" +
                "|海城西|HXT|haichengxi|hcx|922@hde|化德|HGC|huade|hd|923@hdo|洪洞|HDV|hongdong|hd|924@hfe|横峰|HFG|hengfeng" +
                "|hf|925@hfw|韩府湾|HXJ|hanfuwan|hfw|926@hgu|汉沽|HGP|hangu|hg|927@hgy|黄瓜园|HYM|huangguayuan|hgy|928@hgz" +
                "|红光镇|IGW|hongguangzhen|hgz|929@hhg|红花沟|VHD|honghuagou|hhg|930@hht|黄花筒|HUD|huanghuatong|hht|931@hjd" +
                "|贺家店|HJJ|hejiadian|hjd|932@hji|和静|HJR|hejing|hj|933@hji|红江|HFM|hongjiang|hj|934@hji|黑井|HIM|heijing" +
                "|hj|935@hji|获嘉|HJF|huojia|hj|936@hji|河津|HJV|hejin|hj|937@hji|涵江|HJS|hanjiang|hj|938@hjx|河间西|HXP" +
                "|hejianxi|hjx|939@hjz|花家庄|HJM|huajiazhuang|hjz|940@hkn|河口南|HKJ|hekounan|hkn|941@hko|黄口|KOH|huangkou" +
                "|hk|942@hko|湖口|HKG|hukou|hk|943@hla|呼兰|HUB|hulan|hl|944@hlb|葫芦岛北|HPD|huludaobei|hldb|945@hlh|浩良河|HHB" +
                "|haolianghe|hlh|946@hlh|哈拉海|HIT|halahai|hlh|947@hli|鹤立|HOB|heli|hl|948@hli|桦林|HIB|hualin|hl|949@hli" +
                "|黄陵|ULY|huangling|hl|950@hli|海林|HRB|hailin|hl|951@hli|虎林|VLB|hulin|hl|952@hli|寒岭|HAT|hanling|hl|953" +
                "@hlo|和龙|HLL|helong|hl|954@hlo|海龙|HIL|hailong|hl|955@hls|哈拉苏|HAX|harus|hls|956@hlt|呼鲁斯太|VTJ|hulstai" +
                "|hlst|957@hme|黄梅|VEH|huangmei|hm|958@hmt|蛤蟆塘|HMT|hamatang|gmt|959@hmy|韩麻营|HYP|hanmaying|hmy|960@hnh" +
                "|黄泥河|HHL|huangnihe|hnh|961@hni|海宁|HNH|haining|hn|962@hno|惠农|HMJ|huinong|hn|963@hpi|和平|VAQ|heping|hp" +
                "|964@hpz|花棚子|HZM|huapengzi|hpz|965@hqi|花桥|VQH|huaqiao|hq|966@hqi|宏庆|HEY|hongqing|hq|967@hqi|黄|HQW" +
                "|huangqian|h|968@hre|怀仁|HRV|huairen|hr|969@hro|华容|HRN|huarong|hr|970@hsb|华山北|HDY|huashanbei|hsb|971" +
                "@hsd|黄松甸|HDL|huangsongdian|hsd|972@hsg|和什托洛盖|VSR|heshituoluogai|hstlg|973@hsh|红山|VSB|hongshan|hs|974" +
                "@hsh|汉寿|VSQ|hanshou|hs|975";
        public final static String stationString2 = "@hsh|衡山|HSQ|hengshan|hs|976@hsh|黑水|HOT|heishui|hs|977@hsh|惠山|VCH" +
                "|huishan|hs|978@hsh|虎什哈|HHP|hushiha|hsh|979@hsh|猴山|HEP|houshan|hs|980@hsp|红寺堡|HSJ|hongsipu|hsb|981" +
                "@hsw|海石湾|HSO|haishiwan|hsw|982@hsx|衡山西|HEQ|hengshanxi|hsx|983@hsx|红砂岘|VSJ|hongshaxian|hsx|984@hta" +
                "|黑台|HQB|heitai|ht|985@hta|桓台|VTK|huantai|ht|986@hti|和田|VTR|hetian|ht|987@hto|会同|VTQ|huitong|ht|988" +
                "@htz|海坨子|HZT|haituozi|htz|989@hwa|黑旺|HWK|heiwang|hw|990@hwa|海湾|RWH|haiwan|hw|991@hxi|红星|VXB|hongxing" +
                "|hx|992@hxi|徽县|HYY|huixian|hx|993@hxl|红兴隆|VHB|hongxinglong|hxl|994@hxt|换新天|VTB|huanxintian|hxt|995" +
                "@hxt|红岘台|HTJ|hongxiantai|hxt|996@hya|红彦|VIX|hongyan|hy|997@hya|合阳|HAY|heyang|hy|998@hya|海阳|HYK" +
                "|haiyang|hy|999@hyd|衡阳东|HVQ|hengyangdong|hyd|1000@hyi|华蓥|HUW|huaying|hy|1001@hyi|汉阴|HQY|hanyin|hy" +
                "|1002@hyt|黄羊滩|HGJ|huangyangtan|hyt|1003@hyu|汉源|WHW|hanyuan|hy|1004@hyu|河源|VIQ|heyuan|hy|1005@hyu" +
                "|花园|HUN|huayuan|hy|1006@hyw|黄羊湾|HWJ|huangyangwan|hyw|1007@hyz|黄羊镇|HYJ|huangyangzhen|hyz|1008@hzh" +
                "|化州|HZZ|huazhou|hz|1009@hzh|黄州|VON|huangzhou|hz|1010@hzh|霍州|HZV|huozhou|hz|1011@hzx|惠州西|VXQ" +
                "|huizhouxi|hzx|1012@jba|巨宝|JRT|jubao|jb|1013@jbi|靖边|JIY|jingbian|jb|1014@jbt|金宝屯|JBD|jinbaotun|jbt" +
                "|1015@jcb|晋城北|JEF|jinchengbei|jcb|1016@jch|金昌|JCJ|jinchang|jc|1017@jch|鄄城|JCK|juancheng|jc|1018@jch" +
                "|交城|JNV|jiaocheng|jc|1019@jch|建昌|JFD|jianchang|jc|1020@jde|峻德|JDB|junde|jd|1021@jdi|井店|JFP|jingdian" +
                "|jd|1022@jdo|鸡东|JOB|jidong|jd|1023@jdu|江都|UDH|jiangdu|jd|1024@jgs|鸡冠山|JST|jiguanshan|jgs|1025@jgt" +
                "|金沟屯|VGP|jingoutun|jgt|1026@jha|静海|JHP|jinghai|jh|1027@jhe|金河|JHX|jinhe|jh|1028@jhe|锦河|JHB|jinhe|jh" +
                "|1029@jhe|锦和|JHQ|jinhe|jh|1030@jhe|精河|JHR|jinghe|jh|1031@jhn|精河南|JIR|jinghenan|jhn|1032@jhu|江华|JHZ" +
                "|jianghua|jh|1033@jhu|建湖|AJH|jianhu|jh|1034@jjg|纪家沟|VJD|jijiagou|jjg|1035@jji|晋江|JJS|jinjiang|jj" +
                "|1036@jji|江津|JJW|jiangjin|jj|1037@jji|姜家|JJB|jiangjia|jj|1038@jke|金坑|JKT|jinkeng|jk|1039@jli|芨岭|JLJ" +
                "|jiling|jl|1040@jmc|金马村|JMM|jinmacun|jmc|1041@jme|角美|JES|jiaomei|jm|1042@jme|江门|JWQ|jiangmen|jm|1043" +
                "@jna|莒南|JOK|junan|jn|1044@jna|井南|JNP|jingnan|jn|1045@jou|建瓯|JVS|jianou|jo|1046@jpe|经棚|JPC|jingpeng" +
                "|jp|1047@jqi|江桥|JQX|jiangqiao|jq|1048@jsa|九三|SSX|jiusan|js|1049@jsb|金山北|EGH|jinshanbei|jsb|1050@jsh" +
                "|京山|JCN|jingshan|js|1051@jsh|建始|JRN|jianshi|js|1052@jsh|嘉善|JSH|jiashan|js|1053@jsh|稷山|JVV|jishan|js" +
                "|1054@jsh|吉舒|JSL|jishu|js|1055@jsh|建设|JET|jianshe|js|1056@jsh|甲山|JOP|jiashan|js|1057@jsj|建三江|JIB" +
                "|jiansanjiang|jsj|1058@jsn|嘉善南|EAH|jiashannan|jsn|1059@jst|金山屯|JTB|jinshantun|jst|1060@jst|江所田|JOM" +
                "|jiangsuotian|jst|1061@jta|景泰|JTJ|jingtai|jt|1062@jwe|吉文|JWX|jiwen|jw|1063@jxi|进贤|JUG|jinxian|jx" +
                "|1064@jxi|莒县|JKK|juxian|jx|1065@jxi|嘉祥|JUK|jiaxiang|jx|1066@jxi|介休|JXV|jiexiu|jx|1067@jxi|井陉|JJP" +
                "|jingxing|jx|1068@jxi|嘉兴|JXH|jiaxing|jx|1069@jxn|嘉兴南|EPH|jiaxingnan|jxn|1070@jxz|夹心子|JXT|jiaxinzi" +
                "|jxz|1071@jya|简阳|JYW|jianyang|jy|1072@jya|揭阳|JRQ|jieyang|jy|1073@jya|建阳|JYS|jianyang|jy|1074@jya" +
                "|姜堰|UEH|jiangyan|jy|1075@jye|巨野|JYK|juye|jy|1076@jyo|江永|JYZ|jiangyong|jy|1077@jyu|靖远|JYJ|jingyuan|jy" +
                "|1078@jyu|缙云|JYH|jinyun|jy|1079@jyu|江源|SZL|jiangyuan|jy|1080@jyu|济源|JYF|jiyuan|jy|1081@jyx|靖远西|JXJ" +
                "|jingyuanxi|jyx|1082@jzb|胶州北|JZK|jiaozhoubei|jzb|1083@jzd|焦作东|WEF|jiaozuodong|jzd|1084@jzh|靖州|JEQ" +
                "|jingzhou|jz|1085@jzh|荆州|JBN|jingzhou|jz|1086@jzh|金寨|JZH|jinzhai|jz|1087@jzh|晋州|JXP|jinzhou|jz|1088" +
                "@jzh|胶州|JXK|jiaozhou|jz|1089@jzn|锦州南|JOD|jinzhounan|jzn|1090@jzu|焦作|JOF|jiaozuo|jz|1091@jzw|旧庄窝|JVP" +
                "|jiuzhuangwo|jzw|1092@jzz|金杖子|JYD|jinzhangzi|jzz|1093@kan|开安|KAT|kaian|ka|1094@kch|库车|KCR|kuche|kc" +
                "|1095@kch|康城|KCP|kangcheng|kc|1096@kde|库都尔|KDX|huder|kde|1097@kdi|宽甸|KDT|kuandian|kd|1098@kdo|克东|KOB" +
                "|kedong|kd|1099@kjj|康金井|KJB|kangjinjing|kjj|1100@klq|喀喇其|KQX|kalaqi|klq|1101@klu|开鲁|KLC|kailu|kl" +
                "|1102@kly|克拉玛依|KHR|kelamayi|klmy|1103@kqi|口前|KQL|kouqian|kq|1104@ksh|奎山|KAB|kuishan|ks|1105@ksh" +
                "|昆山|KSH|kunshan|ks|1106@ksh|克山|KSB|keshan|ks|1107@kto|开通|KTT|kaitong|kt|1108@kxl|康熙岭|KXZ|kangxiling" +
                "|kxl|1109@kyh|克一河|KHX|keyihe|kyh|1110@kyx|开原西|KXT|kaiyuanxi|kyx|1111@kzh|康庄|KZP|kangzhuang|kz|1112" +
                "@lbi|来宾|UBZ|laibin|lb|1113@lbi|老边|LLT|laobian|lb|1114@lbx|灵宝西|LPF|lingbaoxi|lbx|1115@lch|龙川|LUQ" +
                "|longchuan|lc|1116@lch|乐昌|LCQ|lechang|lc|1117@lch|黎城|UCP|licheng|lc|1118@lch|聊城|UCK|liaocheng|lc" +
                "|1119@lcu|蓝村|LCK|lancun|lc|1120@ldo|林东|LRC|lindong|ld|1121@ldu|乐都|LDO|ledu|ld|1122@ldx|梁底下|LDP" +
                "|liangdixia|ldx|1123@ldz|六道河子|LVP|liudaohezi|ldhz|1124@lfa|鲁番|LVM|lufan|lf|1125@lfa|廊坊|LJP|langfang" +
                "|lf|1126@lfa|落垡|LOP|luofa|lf|1127@lfb|廊坊北|LFP|langfangbei|lfb|1128@lfe|禄丰|LFM|lufeng|lf|1129@lfu" +
                "|老府|UFD|laofu|lf|1130@lga|兰岗|LNB|langang|lg|1131@lgd|龙骨甸|LGM|longgudian|lgd|1132@lgo|芦沟|LOM|lugou|lg" +
                "|1133@lgo|龙沟|LGJ|longgou|lg|1134@lgu|拉古|LGB|lagu|lg|1135@lha|临海|UFH|linhai|lh|1136@lha|林海|LXX|linhai" +
                "|lh|1137@lha|拉哈|LHX|laha|lh|1138@lha|凌海|JID|linghai|lh|1139@lhe|柳河|LNL|liuhe|lh|1140@lhe|六合|KLH|luhe" +
                "|lh|1141@lhu|龙华|LHP|longhua|lh|1142@lhy|滦河沿|UNP|luanheyan|lhy|1143@lhz|六合镇|LEX|liuhezhen|lhz|1144" +
                "@ljd|亮甲店|LRT|liangjiadian|ljd|1145@ljd|刘家店|UDT|liujiadian|ljd|1146@ljh|刘家河|LVT|liujiahe|ljh|1147@lji" +
                "|连江|LKS|lianjiang|lj|1148@lji|李家|LJB|lijia|lj|1149@lji|罗江|LJW|luojiang|lj|1150@lji|廉江|LJZ|lianjiang" +
                "|lj|1151@lji|庐江|UJH|lujiang|lj|1152@lji|励家|LID|lijia|lj|1153@lji|两家|UJT|liangjia|lj|1154@lji|龙江|LJX" +
                "|longjiang|lj|1155@lji|龙嘉|UJL|longjia|lj|1156@ljk|莲江口|LHB|lianjiangkou|ljk|1157@ljl|蔺家楼|ULK" +
                "|linjialou|ljl|1158@ljp|李家坪|LIJ|lijiaping|ljp|1159@ljz|柳家庄|LKJ|liujiazhuang|ljz|1160@lka|兰考|LKF" +
                "|lankao|lk|1161@lko|林口|LKB|linkou|lk|1162@lkp|路口铺|LKQ|lukoupu|lkp|1163@lla|老莱|LAX|laolai|ll|1164@lli" +
                "|拉林|LAB|lalin|ll|1165@lli|陆良|LRM|luliang|ll|1166@lli|龙里|LLW|longli|ll|1167@lli|零陵|UWZ|lingling|ll" +
                "|1168@lli|临澧|LWQ|linli|ll|1169@lli|兰棱|LLB|lanling|ll|1170@llo|卢龙|UAP|lulong|ll|1171@lmd|喇嘛甸|LMX" +
                "|lamadian|lmd|1172@lmd|里木店|LMB|limudian|lmd|1173@lme|洛门|LMJ|luomen|lm|1174@lmh|龙门河|MHA|longmenhe|lmh" +
                "|1175@lmu|栗木|LMN|limu|lm|1176@lna|龙南|UNG|longnan|ln|1177@lpi|梁平|UQW|liangping|lp|1178@lpi|罗平|LPM" +
                "|luoping|lp|1179@lpl|落坡岭|LPP|luopoling|lpl|1180@lps|六盘山|UPJ|liupanshan|lps|1181@lps|乐平市|LPG" +
                "|lepingshi|lps|1182@lqi|临清|UQK|linqing|lq|1183@lqs|龙泉寺|UQJ|longquansi|lqs|1184@lsc|乐善村|LUM|leshancun" +
                "|lsc|1185@lsd|冷水江东|UDQ|lengshuijiangdong|lsjd|1186@lsg|连山关|LGT|lianshanguan|lsg|1187@lsg|流水沟|USP" +
                "|liushuigou|lsg|1188@lsh|陵水|LIQ|lingshui|ls|1189@lsh|乐山|UTW|leshan|ls|1190@lsh|罗山|LRN|luoshan|ls" +
                "|1191@lsh|鲁山|LAF|lushan|ls|1192@lsh|丽水|USH|lishui|ls|1193@lsh|梁山|LMK|liangshan|ls|1194@lsh|灵石|LSV" +
                "|lingshi|ls|1195@lsh|露水河|LUL|lushuihe|lsh|1196@lsh|庐山|LSG|lushan|ls|1197@lsp|林盛堡|LBT|linshengpu|lsb" +
                "|1198@lst|柳树屯|LSD|liushutun|lst|1199@lsz|梨树镇|LSB|lishuzhen|lsz|1200@lsz|龙山镇|LAS|longshanzhen|lsz" +
                "|1201@lsz|李石寨|LET|lishizhai|lsz|1202@lta|黎塘|LTZ|litang|lt|1203@lta|轮台|LAR|luntai|lt|1204@lta|芦台|LTP" +
                "|lutai|lt|1205@ltb|龙塘坝|LBM|longtangba|ltb|1206@ltu|濑湍|LVZ|laituan|lt|1207@ltx|骆驼巷|LTJ|luotuoxiang" +
                "|ltx|1208@lwa|李旺|VLJ|liwang|lw|1209@lwd|莱芜东|LWK|laiwudong|lwd|1210@lws|狼尾山|LRJ|langweishan|lws|1211" +
                "@lwu|灵武|LNJ|lingwu|lw|1212@lwx|莱芜西|UXK|laiwuxi|lwx|1213@lxi|朗乡|LXB|langxiang|lx|1214@lxi|陇县|LXY" +
                "|longxian|lx|1215@lxi|临湘|LXQ|linxiang|lx|1216@lxi|莱西|LXK|laixi|lx|1217@lxi|林西|LXC|linxi|lx|1218@lxi" +
                "|滦县|UXP|luanxian|lx|1219@lya|略阳|LYY|lueyang|ly|1220@lya|莱阳|LYK|laiyang|ly|1221@lya|辽阳|LYT|liaoyang" +
                "|ly|1222@lyb|临沂北|UYK|linyibei|lyb|1223@lyd|凌源东|LDD|lingyuandong|lyd|1224@lyg|连云港|UIH|lianyungang|lyg" +
                "|1225@lyh|老羊壕|LYC|laoyanghao|lyh|1226@lyi|临颍|LNF|linying|ly|1227@lyi|老营|LXL|laoying|ly|1228@lyo" +
                "|龙游|LMH|longyou|ly|1229@lyu|罗源|LVS|luoyuan|ly|1230@lyu|林源|LYX|linyuan|ly|1231@lyu|涟源|LAQ|lianyuan|ly" +
                "|1232@lyu|涞源|LYP|laiyuan|ly|1233@lyx|耒阳西|LPQ|leiyangxi|lyx|1234@lze|临泽|LEJ|linze|lz|1235@lzg|龙爪沟|LZT" +
                "|longzhaogou|lzg|1236@lzh|雷州|UAQ|leizhou|lz|1237@lzh|六枝|LIW|liuzhi|lz|1238@lzh|鹿寨|LIZ|luzhai|lz|1239" +
                "@lzh|来舟|LZS|laizhou|lz|1240@lzh|龙镇|LZA|longzhen|lz|1241@lzh|拉鲊|LEM|lazha|ls|1242@man|明安|MAC|mingan" +
                "|ma|1243@mas|马鞍山|MAH|maanshan|mas|1244@mba|毛坝|MBY|maoba|mb|1245@mbg|毛坝关|MGY|maobaguan|mbg|1246@mcb" +
                "|麻城北|MBN|machengbei|mcb|1247@mch|渑池|MCF|mianchi|mc|1248@mch|明城|MCL|mingcheng|mc|1249@mch|庙城|MAP" +
                "|miaocheng|mc|1250@mcn|渑池南|MNF|mianchinan|mcn|1251@mcp|茅草坪|KPM|maocaoping|mcp|1252@mdh|猛洞河|MUQ" +
                "|mengdonghe|mdh|1253@mds|磨刀石|MOB|modaoshi|mds|1254@mdu|弥渡|MDF|midu|md|1255@mes|帽儿山|MRB|maoershan|mes" +
                "|1256@mga|明港|MGN|minggang|mg|1257@mhk|梅河口|MHL|meihekou|mhk|1258@mhu|马皇|MHZ|mahuang|mh|1259@mjg" +
                "|孟家岗|MGB|mengjiagang|mjg|1260@mla|美兰|MHQ|meilan|ml|1261@mld|汨罗东|MQQ|miluodong|mld|1262@mlh|马莲河|MHB" +
                "|malianhe|mlh|1263@mli|茅岭|MLZ|maoling|ml|1264@mli|庙岭|MLL|miaoling|ml|1265@mli|穆棱|MLB|muling|ml|1266" +
                "@mli|马林|MID|malin|ml|1267@mlo|马龙|MGM|malong|ml|1268@mlo|汨罗|MLQ|miluo|ml|1269@mlt|木里图|MUD|mulitu|mlt" +
                "|1270@mml|密马龙|MMM|mimalong|mml|1271@mni|冕宁|UGW|mianning|mn|1272@mpa|沐滂|MPQ|mupang|mp|1273@mqh" +
                "|马桥河|MQB|maqiaohe|mqh|1274@mqi|闽清|MQS|minqing|mq|1275@mqu|民权|MQF|minquan|mq|1276@msh|明水河|MUT" +
                "|mingshuihe|msh|1277@msh|麻山|MAB|mashan|ms|1278@msh|眉山|MSW|meishan|ms|1279@msw|漫水湾|MKW|manshuiwan|msw" +
                "|1280@msz|茂舍祖|MOM|maoshezu|msz|1281@msz|米沙子|MST|mishazi|msz|1282@mtz|庙台子|MZB|miaotaizi|mtz|1283@mxi" +
                "|美溪|MEB|meixi|mx|1284@mxi|勉县|MVY|mianxian|mx|1285@mya|麻阳|MVQ|mayang|my|1286@myc|牧羊村|MCM|muyangcun" +
                "|myc|1287@myi|米易|MMW|miyi|my|1288@myu|麦园|MYS|maiyuan|my|1289@myu|墨玉|MUR|moyu|my|1290@myu|密云|MUP" +
                "|miyun|my|1291@mzh|庙庄|MZJ|miaozhuang|mz|1292@mzh|米脂|MEY|mizhi|mz|1293@nan|宁安|NAB|ningan|na|1294@nan" +
                "|农安|NAT|nongan|na|1295@nbs|南博山|NBK|nanboshan|nbs|1296@nch|南仇|NCK|nanchou|nc|1297@ncs|南城司|NSP" +
                "|nanchengsi|ncs|1298@ncu|宁村|NCZ|ningcun|nc|1299@nde|宁德|NES|ningde|nd|1300@ngc|南观村|NGP|nanguancun|ngc" +
                "|1301@ngd|南宫东|NFP|nangongdong|ngd|1302@ngl|南关岭|NLT|nanguanling|ngl|1303@ngu|宁国|NNH|ningguo|ng|1304" +
                "@nha|宁海|NHH|ninghai|nh|1305@nhc|南河川|NHJ|nanhechuan|nhc|1306@nhu|南华|NHS|nanhua|nh|1307@nhy|闹海营|NHP" +
                "|naohaiying|nhy|1308@nji|宁家|NVT|ningjia|nj|1309@nji|牛家|NJB|niujia|nj|1310@nji|南靖|NJS|nanjing|nj|1311" +
                "@nji|能家|NJD|nengjia|nj|1312@nko|南口|NKP|nankou|nk|1313@nkq|南口前|NKT|nankouqian|nkq|1314@nla|南朗|NNQ" +
                "|nanlang|nl|1315@nli|乃林|NLD|nailin|nl|1316@nlk|尼勒克|NIR|nileke|nlk|1317@nlu|那罗|ULZ|naluo|nl|1318@nlx" +
                "|宁陵县|NLF|ninglingxian|nlx|1319@nma|奈曼|NMD|naiman|nm|1320@nmi|宁明|NMZ|ningming|nm|1321@nmu|南木|NMX" +
                "|nanmu|nm|1322@npn|南平南|NNS|nanpingnan|npn|1323@npu|那铺|NPZ|napu|np|1324@nqi|南桥|NQD|nanqiao|nq|1325" +
                "@nqu|那曲|NQO|naqu|nq|1326@nqu|暖泉|NQJ|nuanquan|nq|1327@nta|南台|NTT|nantai|nt|1328@nto|南头|NOQ|nantou|nt" +
                "|1329@nwu|宁武|NWV|ningwu|nw|1330@nwz|南湾子|NWP|nanwanzi|nwz|1331@nxb|南翔北|NEH|nanxiangbei|nxb|1332@nxi" +
                "|宁乡|NXQ|ningxiang|nx|1333@nxi|内乡|NXF|neixiang|nx|1334@nxt|牛心台|NXT|niuxintai|nxt|1335@nyu|南峪|NUP" +
                "|nanyu|ny|1336@nzg|娘子关|NIP|niangziguan|nzg|1337@nzh|南召|NAF|nanzhao|nz|1338@nzm|南杂木|NZT|nanzamu|nzm" +
                "|1339@pan|平安|PAL|pingan|pa|1340@pan|蓬安|PAW|pengan|pa|1341@pay|平安驿|PNO|pinganyi|pay|1342@paz|磐安镇|PAJ" +
                "|pananzhen|paz|1343@paz|平安镇|PZT|pinganzhen|paz|1344@pcd|蒲城东|PEY|puchengdong|pcd|1345@pch|蒲城|PCY" +
                "|pucheng|pc|1346@pde|裴德|PDB|peide|pd|1347@pdi|偏店|PRP|piandian|pd|1348@pdx|平顶山西|BFF|pingdingshanxi" +
                "|pdsx|1349@pdx|坡底下|PXJ|podixia|pdx|1350@pet|瓢儿屯|PRT|piaoertun|pet|1351@pfa|平房|PFB|pingfang|pf|1352" +
                "@pgu|平关|PGM|pingguan|pg|1353@pgu|盘关|PAM|panguan|pg|1354@pgu|平果|PGZ|pingguo|pg|1355@phb|徘徊北|PHP" +
                "|paihuibei|phb|1356@phk|平河口|PHM|pinghekou|phk|1357@pjb|盘锦北|PBD|panjinbei|pjb|1358@pjd|潘家店|PDP" +
                "|panjiadian|pjd|1359@pko|皮口|PKT|pikou|pk|1360@pld|普兰店|PLT|pulandian|pld|1361@pli|偏岭|PNT|pianling|pl" +
                "|1362@psh|平山|PSB|pingshan|ps|1363@psh|彭山|PSW|pengshan|ps|1364@psh|皮山|PSR|pishan|ps|1365@psh|彭水|PHW" +
                "|pengshui|ps|1366@psh|磐石|PSL|panshi|ps|1367@psh|平社|PSV|pingshe|ps|1368@pta|平台|PVT|pingtai|pt|1369" +
                "@pti|平田|PTM|pingtian|pt|1370@pti|莆田|PTS|putian|pt|1371@ptq|葡萄菁|PTW|putaoqing|ptj|1372@pwa|平旺|PWV" +
                "|pingwang|pw|1373@pwa|普湾|PWT|puwan|pw|1374@pxi|普雄|POW|puxiong|px|1375@pya|平洋|PYX|pingyang|py|1376" +
                "@pya|彭阳|PYJ|pengyang|py|1377@pya|平遥|PYV|pingyao|py|1378@pyi|平邑|PIK|pingyi|py|1379@pyp|平原堡|PPJ" +
                "|pingyuanpu|pyb|1380@pyu|平原|PYK|pingyuan|py|1381@pyu|平峪|PYP|pingyu|py|1382@pze|彭泽|PZG|pengze|pz|1383" +
                "@pzh|邳州|PJH|pizhou|pz|1384@pzh|平庄|PZD|pingzhuang|pz|1385@pzi|泡子|POD|paozi|pz|1386@pzn|平庄南|PND" +
                "|pingzhuangnan|pzn|1387@qan|乾安|QOT|qianan|qa|1388@qan|庆安|QAB|qingan|qa|1389@qan|迁安|QQP|qianan|qa" +
                "|1390@qdi|七甸|QDM|qidian|qd|1391@qdo|祁东|QRQ|qidong|qd|1392@qfd|曲阜东|QAK|qufudong|qfd|1393@qfe|庆丰|QFT" +
                "|qingfeng|qf|1394@qft|奇峰塔|QVP|qifengta|qft|1395@qfu|曲阜|QFK|qufu|qf|1396@qfy|勤丰营|QFM|qinfengying|qfy" +
                "|1397@qha|琼海|QYQ|qionghai|qh|1398@qhd|秦皇岛|QTP|qinhuangdao|qhd|1399@qhe|千河|QUY|qianhe|qh|1400@qhe" +
                "|清河|QIP|qinghe|qh|1401@qhm|清河门|QHD|qinghemen|qhm|1402@qhy|清华园|QHP|qinghuayuan|qhy|1403@qji|渠旧|QJZ" +
                "|qujiu|qj|1404@qji|綦江|QJW|qijiang|qj|1405@qji|潜江|QJN|qianjiang|qj|1406@qji|全椒|INH|quanjiao|qj|1407" +
                "@qji|秦家|QJB|qinjia|qj|1408@qjp|祁家堡|QBT|qijiapu|qjb|1409@qjx|清涧县|QNY|qingjianxian|qjx|1410@qjz" +
                "|秦家庄|QZV|qinjiazhuang|qjz|1411@qlh|七里河|QLD|qilihe|qlh|1412@qli|渠黎|QLZ|quli|ql|1413@qli|秦岭|QLY" +
                "|qinling|ql|1414@qls|青龙山|QGH|qinglongshan|qls|1415@qls|青龙寺|QSM|qinglongsi|qls|1416@qme|祁门|QIH|qimen" +
                "|qm|1417@qmt|前磨头|QMP|qianmotou|qmt|1418@qsh|青山|QSB|qingshan|qs|1419@qsh|全胜|QVB|quansheng|qs|1420@qsh" +
                "|确山|QSN|queshan|qs|1421@qsh|清水|QUJ|qingshui|qs|1422@qsy|戚墅堰|QYH|qishuyan|qsy|1423@qti|青田|QVH" +
                "|qingtian|qt|1424@qto|桥头|QAT|qiaotou|qt|1425@qtx|青铜峡|QTJ|qingtongxia|qtx|1426@qwt|前苇塘|QWP" +
                "|qianweitang|qwt|1427@qxi|渠县|QRW|quxian|qx|1428@qxi|祁县|QXV|qixian|qx|1429@qxi|青县|QXP|qingxian|qx" +
                "|1430@qxi|桥西|QXJ|qiaoxi|qx|1431@qxu|清徐|QUV|qingxu|qx|1432@qxy|旗下营|QXC|qixiaying|qxy|1433@qya|千阳|QOY" +
                "|qianyang|qy|1434@qya|祁阳|QVQ|qiyang|qy|1435@qya|沁阳|QYF|qinyang|qy|1436@qya|泉阳|QYL|quanyang|qy|1437" +
                "@qyi|七营|QYJ|qiying|qy|1438@qys|庆阳山|QSJ|qingyangshan|qys|1439@qyu|清远|QBQ|qingyuan|qy|1440@qyu|清原|QYT" +
                "|qingyuan|qy|1441@qzd|钦州东|QDZ|qinzhoudong|qzd|1442@qzh|全州|QZZ|quanzhou|qz|1443@qzh|钦州|QRZ|qinzhou|qz" +
                "|1444@qzs|青州市|QZK|qingzhoushi|qzs|1445@ran|瑞安|RAH|ruian|ra|1446@rch|荣昌|RCW|rongchang|rc|1447@rch" +
                "|瑞昌|RCG|ruichang|rc|1448@rga|如皋|RBH|rugao|rg|1449@rgu|容桂|RUQ|ronggui|rg|1450@rqi|任丘|RQP|renqiu|rq" +
                "|1451@rsh|乳山|ROK|rushan|rs|1452@rsh|融水|RSZ|rongshui|rs|1453@rsh|热水|RSD|reshui|rs|1454@rxi|容县|RXZ" +
                "|rongxian|rx|1455@rya|饶阳|RVP|raoyang|ry|1456@rya|汝阳|RYF|ruyang|ry|1457@ryh|绕阳河|RHD|raoyanghe|ryh" +
                "|1458@rzh|汝州|ROF|ruzhou|rz|1459@sba|石坝|OBJ|shiba|sb|1460@sbc|上板城|SBP|shangbancheng|sbc|1461@sbi" +
                "|施秉|AQW|shibing|sb|1462@sbn|上板城南|OBP|shangbanchengnan|sbcn|1463@scb|双城北|SBB|shuangchengbei|scb|1464" +
                "@sch|商城|SWN|shangcheng|sc|1465@sch|莎车|SCR|shache|sc|1466@sch|顺昌|SCS|shunchang|sc|1467@sch|舒城|OCH" +
                "|shucheng|sc|1468@sch|神池|SMV|shenchi|sc|1469@sch|沙城|SCP|shacheng|sc|1470@sch|石城|SCT|shicheng|sc|1471" +
                "@scz|山城镇|SCL|shanchengzhen|scz|1472@sda|山丹|SDJ|shandan|sd|1473@sde|顺德|ORQ|shunde|sd|1474@sde|绥德|ODY" +
                "|suide|sd|1475@sdo|邵东|SOQ|shaodong|sd|1476@sdo|水洞|SIL|shuidong|sd|1477@sdu|商都|SXC|shangdu|sd|1478" +
                "@sdu|十渡|SEP|shidu|sd|1479@sdw|四道湾|OUD|sidaowan|sdw|1480@sfa|绅坊|OLH|shenfang|sf|1481@sfe|双丰|OFB" +
                "|shuangfeng|sf|1482@sft|四方台|STB|sifangtai|sft|1483@sfu|水富|OTW|shuifu|sf|1484@sgk|三关口|OKJ|sanguankou" +
                "|sgk|1485@sgl|桑根达来|OGC|sanggendalai|sgdl|1486@sgu|韶关|SNQ|shaoguan|sg|1487@sgz|上高镇|SVK|shanggaozhen" +
                "|sgz|1488@sha|上杭|JBS|shanghang|sh|1489@sha|沙海|SED|shahai|sh|1490@she|松河|SBM|songhe|sh|1491@she" +
                "|沙河|SHP|shahe|sh|1492@shk|沙河口|SKT|shahekou|shk|1493@shl|赛汗塔拉|SHC|saihantai|shtl|1494@shs|沙河市|VOP" +
                "|shaheshi|shs|1495@sht|山河屯|SHL|shanhetun|sht|1496@shx|三河县|OXP|sanhexian|shx|1497@shy|四合永|OHD" +
                "|siheyong|shy|1498@shz|三汇镇|OZW|sanhuizhen|shz|1499@shz|双河镇|SEL|shuanghezhen|shz|1500@shz|石河子|SZR" +
                "|shihezi|shz|1501@shz|三合庄|SVP|sanhezhuang|shz|1502@sjd|三家店|ODP|sanjiadian|sjd|1503@sjh|水家湖|SQH" +
                "|shuijiahu|sjh|1504@sjh|沈家河|OJJ|shenjiahe|sjh|1505@sjh|松江河|SJL|songjianghe|sjh|1506@sji|尚家|SJB" +
                "|shangjia|sj|1507@sji|孙家|SUB|sunjia|sj|1508@sji|沈家|OJB|shenjia|sj|1509@sji|松江|SAH|songjiang|sj|1510" +
                "@sjk|三江口|SKD|sanjiangkou|sjk|1511@sjl|司家岭|OLK|sijialing|sjl|1512@sjn|松江南|IMH|songjiangnan|sjn|1513" +
                "@sjn|石景山南|SRP|shijingshannan|sjsn|1514@sjt|邵家堂|SJJ|shaojiatang|sjt|1515@sjx|三江县|SOZ|sanjiangxian|sjx" +
                "|1516@sjz|三家寨|SMM|sanjiazhai|sjz|1517@sjz|十家子|SJD|shijiazi|sjz|1518@sjz|松江镇|OZL|songjiangzhen|sjz" +
                "|1519@sjz|施家嘴|SHM|shijiazui|sjz|1520@sjz|深井子|SWT|shenjingzi|sjz|1521@sld|什里店|OMP|shilidian|sld|1522" +
                "@sle|疏勒|SUR|shule|sl|1523@slh|疏勒河|SHJ|shulehe|slh|1524@slh|舍力虎|VLD|shelihu|slh|1525@sli|石磷|SPB" +
                "|shilin|sl|1526@sli|绥棱|SIB|suiling|sl|1527@sli|石岭|SOL|shiling|sl|1528@sli|石林|SLM|shilin|sl|1529@sln" +
                "|石林南|LNM|shilinnan|sln|1530@slo|石龙|SLQ|shilong|sl|1531@slq|萨拉齐|SLC|salaqi|slq|1532@slu|索伦|SNT|suolun" +
                "|sl|1533@slu|商洛|OLY|shangluo|sl|1534@slz|沙岭子|SLP|shalingzi|slz|1535@sme|思|OMW|simeng|s|1536@smn" +
                "|三门峡南|SCF|sanmenxianan|smxn|1537@smx|三门县|OQH|sanmenxian|smx|1538@smx|石门县|OMQ|shimenxian|smx|1539@smx" +
                "|三门峡西|SXF|sanmenxiaxi|smxx|1540@sni|肃宁|SYP|suning|sn|1541@son|宋|SOB|song|s|1542@spa|双牌|SBZ|shuangpai" +
                "|sp|1543@spd|四平东|PPT|sipingdong|spd|1544@spi|遂平|SON|suiping|sp|1545@spt|沙坡头|SFJ|shapotou|spt|1546" +
                "@sqn|商丘南|SPF|shangqiunan|sqn|1547@squ|水泉|SID|shuiquan|sq|1548@sqx|石泉县|SXY|shiquanxian|sqx|1549@sqz" +
                "|石桥子|SQT|shiqiaozi|sqz|1550@src|石人城|SRB|shirencheng|src|1551@sre|石人|SRL|shiren|sr|1552@ssh|山市|SQB" +
                "|shanshi|ss|1553@ssh|神树|SWB|shenshu|ss|1554@ssh|鄯善|SSR|shanshan|ss|1555@ssh|三水|SJQ|sanshui|ss|1556" +
                "@ssh|泗水|OSK|sishui|ss|1557@ssh|松树|SFT|songshu|ss|1558@ssh|首山|SAT|shoushan|ss|1559@ssj|三十家|SRD" +
                "|sanshijia|ssj|1560@ssp|三十里堡|SST|sanshilipu|sslb|1561@ssz|松树镇|SSL|songshuzhen|ssz|1562@sta|松桃|MZQ" +
                "|songtao|st|1563@sth|索图罕|SHX|suotuhan|sth|1564@stj|三堂集|SDH|santangji|stj|1565@sto|石头|OTB|shitou|st" +
                "|1566@sto|神头|SEV|shentou|st|1567@stu|沙沱|SFM|shatuo|st|1568@swa|上万|SWP|shangwan|sw|1569@swu|孙吴|SKB" +
                "|sunwu|sw|1570@swx|沙湾县|SXR|shawanxian|swx|1571@sxi|遂溪|SXZ|suixi|sx|1572@sxi|沙县|SAS|shaxian|sx|1573" +
                "@sxi|绍兴|SOH|shaoxing|sx|1574@sxi|歙县|OVH|shexian|sx|1575@sxp|上西铺|SXM|shangxipu|sxp|1576@sxz|石峡子|SXJ" +
                "|shixiazi|sxz|1577@sya|绥阳|SYB|suiyang|sy|1578@sya|沭阳|FMH|shuyang|sy|1579@sya|寿阳|SYV|shouyang|sy|1580" +
                "@sya|水洋|OYP|shuiyang|sy|1581@syc|三阳川|SYJ|sanyangchuan|syc|1582@syd|上腰墩|SPJ|shangyaodun|syd|1583@syi" +
                "|三营|OEJ|sanying|sy|1584@syi|顺义|SOP|shunyi|sy|1585@syj|三义井|OYD|sanyijing|syj|1586@syp|三源浦|SYL" +
                "|sanyuanpu|syp|1587@syu|三原|SAY|sanyuan|sy|1588@syu|上虞|BDH|shangyu|sy|1589@syu|上园|SUD|shangyuan|sy" +
                "|1590@syu|水源|OYJ|shuiyuan|sy|1591@syz|桑园子|SAJ|sangyuanzi|syz|1592@szb|绥中北|SND|suizhongbei|szb|1593" +
                "@szb|苏州北|OHH|suzhoubei|szb|1594@szd|宿州东|SRH|suzhoudong|szd|1595@szd|深圳东|BJQ|shenzhendong|szd|1596" +
                "@szh|深州|OZP|shenzhou|sz|1597@szh|孙镇|OZY|sunzhen|sz|1598@szh|绥中|SZD|suizhong|sz|1599@szh|尚志|SZB" +
                "|shangzhi|sz|1600@szh|师庄|SNM|shizhuang|sz|1601@szi|松滋|SIN|songzi|sz|1602@szo|师宗|SEM|shizong|sz|1603" +
                "@szq|苏州园区|KAH|suzhouyuanqu|szyq|1604@szq|苏州新区|ITH|suzhouxinqu|szxq|1605@szs|石嘴山|SZJ|shizuishan|szs" +
                "|1606@tan|泰安|TMK|taian|ta|1607@tan|台安|TID|taian|ta|1608@tay|通安驿|TAJ|tonganyi|tay|1609@tba|桐柏|TBF" +
                "|tongbai|tb|1610@tbe|通北|TBB|tongbei|tb|1611@tch|汤池|TCX|tangchi|tc|1612@tch|桐城|TTH|tongcheng|tc|1613" +
                "@tch|郯城|TZK|tancheng|tc|1614@tch|铁厂|TCL|tiechang|tc|1615@tcu|桃村|TCK|taocun|tc|1616@tda|通道|TRQ" +
                "|tongdao|td|1617@tdo|田东|TDZ|tiandong|td|1618@tga|天岗|TGL|tiangang|tg|1619@tgl|土贵乌拉|TGC|togrogul|tgwl" +
                "|1620@tgu|太谷|TGV|taigu|tg|1621@tha|塔哈|THX|taha|th|1622@tha|棠海|THM|tanghai|th|1623@the|唐河|THF|tanghe" +
                "|th|1624@the|泰和|THG|taihe|th|1625@thu|太湖|TKH|taihu|th|1626@tji|团结|TIX|tuanjie|tj|1627@tjj|谭家井|TNJ" +
                "|tanjiajing|tjj|1628@tjt|陶家屯|TOT|taojiatun|tjt|1629@tjz|统军庄|TZP|tongjunzhuang|tjz|1630@tka|泰康|TKX" +
                "|taikang|tk|1631@tld|吐列毛杜|TMD|tuliemaodu|tlmd|1632@tlh|图里河|TEX|tulihe|tlh|1633@tli|亭亮|TIZ|tingliang" +
                "|tl|1634@tli|田林|TFZ|tianlin|tl|1635@tli|铜陵|TJH|tongling|tl|1636@tli|铁力|TLB|tieli|tl|1637@tlx|铁岭西|PXT" +
                "|tielingxi|tlx|1638@tme|天门|TMN|tianmen|tm|1639@tmn|天门南|TNN|tianmennan|tmn|1640@tms|太姥山|TLS|taimushan" +
                "|tls|1641@tmt|土牧尔台|TRC|tomortei|tmet|1642@tmz|土门子|TCJ|tumenzi|tmz|1643@tna|潼南|TVW|tongnan|tn|1644" +
                "@tna|洮南|TVT|taonan|tn|1645@tpc|太平川|TIT|taipingchuan|tpc|1646@tpz|太平镇|TEB|taipingzhen|tpz|1647@tqi" +
                "|图强|TQX|tuqiang|tq|1648@tqi|台前|TTK|taiqian|tq|1649@tql|天桥岭|TQL|tianqiaoling|tql|1650@tqz|土桥子|TQJ" +
                "|tuqiaozi|tqz|1651@tsc|汤山城|TCT|tangshancheng|tsc|1652@tsh|桃山|TAB|taoshan|ts|1653@tsz|塔石嘴|TIM" +
                "|tashizui|tsz|1654@twh|汤旺河|THB|tangwanghe|twh|1655@txi|同心|TXJ|tongxin|tx|1656@txi|土溪|TSW|tuxi|tx" +
                "|1657@txi|桐乡|TCH|tongxiang|tx|1658@tya|田阳|TRZ|tianyang|ty|1659@tyi|桃映|TKQ|taoying|ty|1660@tyi|天义|TND" +
                "|tianyi|ty|1661@tyi|汤阴|TYF|tangyin|ty|1662@tyl|驼腰岭|TIL|tuoyaoling|tyl|1663@tys|太阳山|TYJ|taiyangshan" +
                "|tys|1664@tyu|汤原|TYB|tangyuan|ty|1665@tyy|塔崖驿|TYP|tayanyi|tyy|1666@tzd|滕州东|TEK|tengzhoudong|tzd|1667" +
                "@tzh|台州|TZH|taizhou|tz|1668@tzh|天祝|TZJ|tianzhu|tz|1669@tzh|滕州|TXK|tengzhou|tz|1670@tzh|天镇|TZV" +
                "|tianzhen|tz|1671@tzl|桐子林|TEW|tongzilin|tzl|1672@tzs|天柱山|QWH|tianzhushan|tzs|1673@wan|文安|WBP|wenan" +
                "|wa|1674@wan|武安|WAP|wuan|wa|1675@waz|王安镇|WVP|wanganzhen|waz|1676@wcg|五叉沟|WCT|wuchagou|wcg|1677@wch" +
                "|文昌|WEQ|wenchang|wc|1678@wch|温春|WDB|wenchun|wc|1679@wdc|五大连池|WRB|wudalianchi|wdlc|1680@wde|文登|WBK" +
                "|wendeng|wd|1681@wdg|五道沟|WDL|wudaogou|wdg|1682@wdh|五道河|WHP|wudaohe|wdh|1683@wdi|文地|WNZ|wendi|wd|1684" +
                "@wdo|卫东|WVT|weidong|wd|1685@wds|武当山|WRN|wudangshan|wds|1686@wdu|望都|WDP|wangdu|wd|1687@weh|乌尔旗汗|WHX" +
                "|orqohan|weqh|1688@wfa|潍坊|WFK|weifang|wf|1689@wft|万发屯|WFB|wanfatun|wft|1690@wfu|王府|WUT|wangfu|wf" +
                "|1691@wfx|瓦房店西|WXT|wafangdianxi|wfdx|1692@wga|王岗|WGB|wanggang|wg|1693@wgo|武功|WGY|wugong|wg|1694@wgo" +
                "|湾沟|WGL|wangou|wg|1695@wgt|吴官田|WGM|wuguantian|wgt|1696@wha|乌海|WVC|wuhai|wh|1697@whe|苇河|WHB|weihe|wh" +
                "|1698@whu|卫辉|WHF|weihui|wh|1699@wjc|吴家川|WCJ|wujiachuan|wjc|1700@wji|五家|WUB|wujia|wj|1701@wji|威箐|WAM" +
                "|weiqing|wq|1702@wji|午汲|WJP|wuji|wj|1703@wke|倭肯|WQB|woken|wk|1704@wks|五棵树|WKT|wukeshu|wks|1705@wlb" +
                "|五龙背|WBT|wulongbei|wlb|1706@wld|乌兰哈达|WLC|ulanhad|wlhd|1707@wle|万乐|WEB|wanle|wl|1708@wlg|瓦拉干|WVX" +
                "|walagan|wlg|1709@wli|温岭|VHH|wenling|wl|1710@wli|五莲|WLK|wulian|wl|1711@wlq|乌拉特前旗|WQC|uradqranqi" +
                "|wltqq|1712@wls|乌拉山|WSC|wulashan|wls|1713@wlt|卧里屯|WLX|wolitun|wlt|1714@wnb|渭南北|WBY|weinanbei|wnb" +
                "|1715@wne|乌奴耳|WRX|onor|wne|1716@wni|万宁|WNQ|wanning|wn|1717@wni|万年|WWG|wannian|wn|1718@wnn|渭南南|WVY" +
                "|weinannan|wnn|1719@wnz|渭南镇|WNJ|weinanzhen|wnz|1720@wpi|沃皮|WPT|wopi|wp|1721@wpu|吴堡|WUY|wupu|wb|1722" +
                "@wqi|吴桥|WUP|wuqiao|wq|1723@wqi|汪清|WQL|wangqing|wq|1724@wqi|弯|WQW|wanqiu|w|1725@wqi|武清|WWP|wuqing|wq" +
                "|1726@wqu|温泉|WQM|wenquan|wq|1727@wsh|武山|WSJ|wushan|ws|1728@wsh|文水|WEV|wenshui|ws|1729@wsz|魏善庄|WSP" +
                "|weishanzhuang|wsz|1730@wto|王瞳|WTP|wangtong|wt|1731@wts|五台山|WSV|wutaishan|wts|1732@wtz|王团庄|WZJ" +
                "|wangtuanzhuang|wtz|1733@wwu|五五|WVR|wuwu|ww|1734@wxd|无锡东|WGH|wuxidong|wxd|1735@wxi|卫星|WVB|weixing|wx" +
                "|1736@wxi|闻喜|WXV|wenxi|wx|1737@wxi|武乡|WVV|wuxiang|wx|1738@wxq|无锡新区|IFH|wuxixinqu|wxxq|1739@wxu" +
                "|武穴|WXN|wuxue|wx|1740@wxu|吴圩|WYZ|wuxu|wx|1741@wya|王杨|WYB|wangyang|wy|1742@wyi|五营|WWB|wuying|wy|1743" +
                "@wyi|武义|RYH|wuyi|wy|1744@wyt|瓦窑田|WIM|wayaotian|wyt|1745@wyu|五原|WYC|wuyuan|wy|1746@wzg|苇子沟|WZL" +
                "|weizigou|wzg|1747@wzh|韦庄|WZY|weizhuang|wz|1748@wzh|五寨|WZV|wuzhai|wz|1749@wzt|王兆屯|WZB|wangzhaotun" +
                "|wzt|1750@wzz|微子镇|WQP|weizizhen|wzz|1751@wzz|魏杖子|WKD|weizhangzi|wzz|1752@xan|新安|EAM|xinan|xa|1753" +
                "@xan|兴安|XAZ|xingan|xa|1754@xax|新安县|XAF|xinanxian|xax|1755@xba|新保安|XAP|xinbaoan|xba|1756@xbc|下板城|EBP" +
                "|xiabancheng|xbc|1757@xbl|西八里|XLP|xibali|xbl|1758@xch|宣城|ECH|xuancheng|xc|1759@xch|兴城|XCD|xingcheng" +
                "|xc|1760@xcu|小村|XEM|xiaocun|xc|1761@xcy|新绰源|XRX|xinchuoyuan|xcy|1762@xcz|下城子|XCB|xiachengzi|xcz|1763" +
                "@xde|喜德|EDW|xide|xd|1764@xdj|小得江|EJM|xiaodejiang|xdj|1765@xdm|西大庙|XMP|xidamiao|xdm|1766@xdo|小董|XEZ" +
                "|xiaodong|xd|1767@xdo|小东|XOD|xiaodong|xd|1768@xdp|西斗铺|XPC|xidoupu|xdp|1769@xfe|息烽|XFW|xifeng|xf|1770" +
                "@xfe|信丰|EFG|xinfeng|xf|1771@xfe|襄汾|XFV|xiangfen|xf|1772@xga|新干|EGG|xingan|xg|1773@xga|孝感|XGN|xiaogan" +
                "|xg|1774@xgc|西固城|XUJ|xigucheng|xgc|1775@xgy|夏官营|XGJ|xiaguanying|xgy|1776@xgz|西岗子|NBB|xigangzi|xgz" +
                "|1777@xhe|襄河|XXB|xianghe|xh|1778@xhe|新和|XIR|xinhe|xh|1779@xhe|宣和|XWJ|xuanhe|xh|1780@xhj|斜河涧|EEP" +
                "|xiehejian|xhj|1781@xht|新华屯|XAX|xinhuatun|xht|1782@xhu|新华|XHB|xinhua|xh|1783@xhu|新化|EHQ|xinhua|xh" +
                "|1784@xhu|宣化|XHP|xuanhua|xh|1785@xhx|兴和西|XEC|xinghexi|xhx|1786@xhy|小河沿|XYD|xiaoheyan|xhy|1787@xhy" +
                "|下花园|XYP|xiahuayuan|xhy|1788@xhz|小河镇|EKY|xiaohezhen|xhz|1789@xji|徐家|XJB|xujia|xj|1790@xji|新绛|XJV" +
                "|xinjiang|xj|1791@xji|辛集|ENP|xinji|xj|1792@xji|新江|XJM|xinjiang|xj|1793@xjk|西街口|EKM|xijiekou|xjk|1794" +
                "@xjt|许家屯|XJT|xujiatun|xjt|1795@xjt|许家台|XTJ|xujiatai|xjt|1796@xjz|谢家镇|XMT|xiejiazhen|xjz|1797@xka" +
                "|兴凯|EKB|xingkai|xk|1798@xla|小榄|EAQ|xiaolan|xl|1799@xla|香兰|XNB|xianglan|xl|1800@xld|兴隆店|XDD" +
                "|xinglongdian|xld|1801@xle|新乐|ELP|xinle|xl|1802@xli|新林|XPX|xinlin|xl|1803@xli|小岭|XLB|xiaoling|xl" +
                "|1804@xli|新李|XLJ|xinli|xl|1805@xli|西林|XYB|xilin|xl|1806@xli|西柳|GCT|xiliu|xl|1807@xli|仙林|XPH|xianlin" +
                "|xl|1808@xlt|新立屯|XLD|xinlitun|xlt|1809@xlx|小路溪|XLM|xiaoluxi|xlx|1810@xlz|兴隆镇|XZB|xinglongzhen|xlz" +
                "|1811@xlz|新立镇|XGT|xinlizhen|xlz|1812@xmi|新民|XMD|xinmin|xm|1813@xms|西麻山|XMB|ximashan|xms|1814@xmt" +
                "|下马塘|XAT|xiamatang|xmt|1815@xna|孝南|XNV|xiaonan|xn|1816@xnb|咸宁北|XRN|xianningbei|xnb|1817@xni|兴宁|ENQ" +
                "|xingning|xn|1818@xni|咸宁|XNN|xianning|xn|1819@xpi|西平|XPN|xiping|xp|1820@xpi|兴平|XPY|xingping|xp|1821" +
                "@xpt|新坪田|XPM|xinpingtian|xpt|1822@xpu|霞浦|XOS|xiapu|xp|1823@xpu|溆浦|EPQ|xupu|xp|1824@xpu|犀浦|XIW|xipu" +
                "|xp|1825@xqi|新青|XQB|xinqing|xq|1826@xqi|新邱|XQD|xinqiu|xq|1827@xqp|兴泉堡|XQJ|xingquanpu|xqb|1828@xrq" +
                "|仙人桥|XRL|xianrenqiao|xrq|1829@xsg|小寺沟|ESP|xiaosigou|xsg|1830@xsh|杏树|XSB|xingshu|xs|1831@xsh|夏石|XIZ" +
                "|xiashi|xs|1832@xsh|浠水|XZN|xishui|xs|1833@xsh|下社|XSV|xiashe|xs|1834@xsh|徐水|XSP|xushui|xs|1835@xsh" +
                "|小哨|XAM|xiaoshao|xs|1836@xsp|新松浦|XOB|xinsongpu|xsp|1837@xst|杏树屯|XDT|xingshutun|xst|1838@xsw|许三湾|XSJ" +
                "|xusanwan|xsw|1839@xta|滩|XTW|xiantan|t|1840@xta|邢台|XTP|xingtai|xt|1841@xtx|仙桃西|XAN|xiantaoxi|xtx" +
                "|1842@xtz|下台子|EIP|xiataizi|xtz|1843@xwe|徐闻|XJQ|xuwen|xw|1844@xwp|新窝铺|EPD|xinwopu|xwp|1845@xwu|修武|XWF" +
                "|xiuwu|xw|1846@xxi|新县|XSN|xinxian|xx|1847@xxi|息县|ENN|xixian|xx|1848@xxi|西乡|XQY|xixiang|xx|1849@xxi" +
                "|西峡|XIF|xixia|xx|1850@xxi|孝西|XOV|xiaoxi|xx|1851@xxj|小新街|XXM|xiaoxinjie|xxj|1852@xxx|新兴县|XGQ" +
                "|xinxingxian|xxx|1853@xxz|西小召|XZC|xixiaozhao|xxz|1854@xxz|小西庄|XXP|xiaoxizhuang|xxz|1855@xya|向阳|XDB" +
                "|xiangyang|xy|1856@xya|旬阳|XUY|xunyang|xy|1857@xyb|旬阳北|XBY|xunyangbei|xyb|1858@xyd|襄阳东|XWN" +
                "|xiangyangdong|xyd|1859@xye|兴业|SNZ|xingye|xy|1860@xyg|小雨谷|XHM|xiaoyugu|xyg|1861@xyi|信宜|EEQ|xinyi|xy" +
                "|1862@xyj|小月旧|XFM|xiaoyuejiu|xyj|1863@xyq|小扬气|XYX|xiaoyangqi|xyq|1864@xyu|祥云|EXM|xiangyun|xy|1865" +
                "@xyu|襄垣|EIF|xiangyuan|xy|1866@xyx|夏邑县|EJH|xiayixian|xyx|1867@xyy|新友谊|EYB|xinyouyi|xyy|1868@xyz" +
                "|新阳镇|XZJ|xinyangzhen|xyz|1869@xzd|徐州东|UUH|xuzhoudong|xzd|1870@xzf|新帐房|XZX|xinzhangfang|xzf|1871@xzh" +
                "|悬钟|XRP|xuanzhong|xz|1872@xzh|新肇|XZT|xinzhao|xz|1873@xzh|忻州|XXV|xinzhou|xz|1874@xzi|汐子|XZD|xizi|xz" +
                "|1875@xzm|西哲里木|XRD|xizhelimu|xzlm|1876@xzz|新杖子|ERP|xinzhangzi|xzz|1877@yan|姚安|YAC|yaoan|ya|1878@yan" +
                "|依安|YAX|yian|ya|1879@yan|永安|YAS|yongan|ya|1880@yax|永安乡|YNB|yonganxiang|yax|1881@ybc|渔坝村|YBM|yubacun" +
                "|ybc|1882@ybl|亚布力|YBB|yabuli|ybl|1883@ybs|元宝山|YUD|yuanbaoshan|ybs|1884@yca|羊草|YAB|yangcao|yc|1885" +
                "@ycd|秧草地|YKM|yangcaodi|ycd|1886@ych|阳澄湖|AIH|yangchenghu|ych|1887@ych|迎春|YYB|yingchun|yc|1888@ych" +
                "|叶城|YER|yecheng|yc|1889@ych|盐池|YKJ|yanchi|yc|1890@ych|砚川|YYY|yanchuan|yc|1891@ych|阳春|YQQ|yangchun|yc" +
                "|1892@ych|宜城|YIN|yicheng|yc|1893@ych|应城|YHN|yingcheng|yc|1894@ych|禹城|YCK|yucheng|yc|1895@ych|晏城|YEK" +
                "|yancheng|yc|1896@ych|羊场|YED|yangchang|yc|1897@ych|阳城|YNF|yangcheng|yc|1898@ych|阳岔|YAL|yangcha|yc" +
                "|1899@ych|郓城|YPK|yuncheng|yc|1900@ych|雁翅|YAP|yanchi|yc|1901@ycl|云彩岭|ACP|yuncailing|ycl|1902@ycx" +
                "|虞城县|IXH|yuchengxian|ycx|1903@ycz|营城子|YCT|yingchengzi|ycz|1904@yde|永登|YDJ|yongdeng|yd|1905@yde" +
                "|英德|YDQ|yingde|yd|1906@ydi|尹地|YDM|yindi|yd|1907@ydi|永定|YGS|yongding|yd|1908@yds|雁荡山|YGH|yandangshan" +
                "|yds|1909@ydu|于都|YDG|yudu|yd|1910@ydu|园墩|YAJ|yuandun|yd|1911@ydx|英德西|IIQ|yingdexi|ydx|1912@yfu" +
                "|永福|YFZ|yongfu|yf|1913@yfy|永丰营|YYM|yongfengying|yfy|1914@yga|杨岗|YRB|yanggang|yg|1915@yga|阳高|YOV" +
                "|yanggao|yg|1916@ygu|阳谷|YIK|yanggu|yg|1917@yha|友好|YOB|youhao|yh|1918@yha|余杭|EVH|yuhang|yh|1919@yhc" +
                "|沿河城|YHP|yanhecheng|yhc|1920@yhu|岩会|AEP|yanhui|yh|1921@yjh|羊臼河|YHM|yangjiuhe|yjh|1922@yji|永嘉|URH" +
                "|yongjia|yj|1923@yji|盐津|AEW|yanjin|yj|1924@yji|余江|YHG|yujiang|yj|1925@yji|叶集|YCH|yeji|yj|1926@yji" +
                "|燕郊|AJP|yanjiao|yj|1927@yji|姚家|YAT|yaojia|yj|1928@yjj|岳家井|YGJ|yuejiajing|yjj|1929@yjp|一间堡|YJT" +
                "|yijianpu|yjb|1930@yjs|英吉沙|YIR|yingjisha|yjs|1931@yjs|云居寺|AFP|yunjusi|yjs|1932@yjz|燕家庄|AZK" +
                "|yanjiazhuang|yjz|1933@yka|永康|RFH|yongkang|yk|1934@ykd|营口东|YGT|yingkoudong|ykd|1935@yla|银浪|YJX" +
                "|yinlang|yl|1936@yla|永郎|YLW|yonglang|yl|1937@ylb|宜良北|YSM|yiliangbei|ylb|1938@yld|永乐店|YDY|yongledian" +
                "|yld|1939@ylh|伊拉哈|YLX|yilaha|ylh|1940@yli|伊林|YLB|yilin|yl|1941@yli|彝良|ALW|yiliang|yl|1942@yli|杨林|YLM" +
                "|yanglin|yl|1943@ylp|余粮堡|YLD|yuliangpu|ylb|1944@ylq|杨柳青|YQP|yangliuqing|ylq|1945@ylt|月亮田|YUM" +
                "|yueliangtian|ylt|1946@ylw|亚龙湾|TWQ|yalongwan|ylw|1947@ylz|杨陵镇|YSY|yanglingzhen|ylz|1948@yma|义马|YMF" +
                "|yima|ym|1949@yme|云梦|YMN|yunmeng|ym|1950@ymo|元谋|YMM|yuanmou|ym|1951@yms|一面山|YST|yimianshan|yms|1952" +
                "@ymz|玉门镇|YXJ|yumenzhen|ymz|1953@yna|沂南|YNK|yinan|yn|1954@yna|宜耐|YVM|yinai|yn|1955@ynd|伊宁东|YNR" +
                "|yiningdong|ynd|1956@ypl|一平浪|YIM|yipinglang|ypl|1957@yps|营盘水|YZJ|yingpanshui|yps|1958@ypu|羊堡|ABM" +
                "|yangpu|yb|1959@ypw|营盘湾|YPC|yingpanwan|ypw|1960@yqb|阳泉北|YPP|yangquanbei|yqb|1961@yqi|乐清|UPH|yueqing" +
                "|lq|1962@yqi|焉耆|YSR|yanqi|yq|1963@yqi|源迁|AQK|yuanqian|yq|1964@yqt|姚千户屯|YQT|yaoqianhutun|yqht|1965" +
                "@yqu|阳曲|YQV|yangqu|yq|1966@ysg|榆树沟|YGP|yushugou|ysg|1967@ysh|月山|YBF|yueshan|ys|1968@ysh|玉石|YSJ|yushi" +
                "|ys|1969@ysh|偃师|YSF|yanshi|ys|1970@ysh|沂水|YUK|yishui|ys|1971@ysh|榆社|YSV|yushe|ys|1972@ysh|颍上|YVH" +
                "|yingshang|ys|1973@ysh|窑上|ASP|yaoshang|ys|1974@ysh|元氏|YSP|yuanshi|ys|1975@ysl|杨树岭|YAD|yangshuling" +
                "|ysl|1976@ysp|野三坡|AIP|yesanpo|ysp|1977@yst|榆树屯|YSX|yushutun|yst|1978@yst|榆树台|YUT|yushutai|yst|1979" +
                "@ysz|鹰手营子|YIP|yingshouyingzi|ysyz|1980@yta|源潭|YTQ|yuantan|yt|1981@ytp|牙屯堡|YTZ|yatunpu|ytb|1982@yts" +
                "|烟筒山|YSL|yantongshan|yts|1983@ytt|烟筒屯|YUX|yantongtun|ytt|1984@yws|羊尾哨|YWM|yangweishao|yws|1985@yxi" +
                "|越西|YHW|yuexi|yx|1986@yxi|攸县|YOG|youxian|yx|1987@yxi|永修|ACG|yongxiu|yx|1988@yya|酉阳|AFW|youyang|yy" +
                "|1989@yya|余姚|YYH|yuyao|yy|1990@yyd|弋阳东|YIG|yiyangdong|yyd|1991@yyd|岳阳东|YIQ|yueyangdong|yyd|1992@yyi" +
                "|阳邑|ARP|yangyi|yy|1993@yyu|鸭园|YYL|yayuan|yy|1994@yyz|鸳鸯镇|YYJ|yuanyangzhen|yyz|1995@yzb|燕子砭|YZY" +
                "|yanzibian|yzb|1996@yzh|宜州|YSZ|yizhou|yz|1997@yzh|仪征|UZH|yizheng|yz|1998@yzh|兖州|YZK|yanzhou|yz|1999" +
                "@yzi|迤资|YQM|yizi|yz|2000@yzw|羊者窝|AEM|yangzhewo|yzw|2001@yzz|杨杖子|YZD|yangzhangzi|yzz|2002@zan|镇安|ZEY" +
                "|zhenan|za|2003@zan|治安|ZAD|zhian|za|2004@zba|招柏|ZBP|zhaobai|zb|2005@zbw|张百湾|ZUP|zhangbaiwan|zbw|2006" +
                "@zch|枝城|ZCN|zhicheng|zc|2007@zch|子长|ZHY|zichang|zc|2008@zch|诸城|ZQK|zhucheng|zc|2009@zch|邹城|ZIK" +
                "|zoucheng|zc|2010@zch|赵城|ZCV|zhaocheng|zc|2011@zda|章党|ZHT|zhangdang|zd|2012@zdo|肇东|ZDB|zhaodong|zd" +
                "|2013@zfp|照福铺|ZFM|zhaofupu|zfp|2014@zgt|章古台|ZGD|zhanggutai|zgt|2015@zgu|赵光|ZGB|zhaoguang|zg|2016@zhe" +
                "|中和|ZHX|zhonghe|zh|2017@zhm|中华门|VNH|zhonghuamen|zhm|2018@zjb|枝江北|ZIN|zhijiangbei|zjb|2019@zjc" +
                "|钟家村|ZJY|zhongjiacun|zjc|2020@zjg|朱家沟|ZUB|zhujiagou|zjg|2021@zjg|紫荆关|ZYP|zijingguan|zjg|2022@zji" +
                "|周家|ZOB|zhoujia|zj|2023@zji|诸暨|ZDH|zhuji|zj|2024@zjn|镇江南|ZEH|zhenjiangnan|zjn|2025@zjt|周家屯|ZOD" +
                "|zhoujiatun|zjt|2026@zjt|郑家屯|ZJD|zhengjiatun|zjt|2027@zjw|褚家湾|CWJ|zhujiawan|cjw|2028@zjx|湛江西|ZWQ" +
                "|zhanjiangxi|zjx|2029@zjy|朱家窑|ZUJ|zhujiayao|zjy|2030@zjz|曾家坪子|ZBW|caojiapingzi|zjpz|2031@zla|镇赉|ZLT" +
                "|zhenlai|zl|2032@zli|枣林|ZIV|zaolin|zl|2033@zlt|扎鲁特|ZLD|zhalute|zlt|2034@zlx|扎赉诺尔西|ZXX|jalainurxi" +
                "|zlnex|2035@zmt|樟木头|ZOQ|zhangmutou|zmt|2036@zmu|中牟|ZGF|zhongmu|zm|2037@znd|中宁东|ZDJ|zhongningdong|znd" +
                "|2038@zni|中宁|VNJ|zhongning|zn|2039@znn|中宁南|ZNJ|zhongningnan|znn|2040@zpi|镇平|ZPF|zhenping|zp|2041@zpi" +
                "|漳平|ZPS|zhangping|zp|2042@zpu|泽普|ZPR|zepu|zp|2043@zqi|枣强|ZVP|zaoqiang|zq|2044@zqi|张桥|ZQY|zhangqiao" +
                "|zq|2045@zqi|章丘|ZTK|zhangqiu|zq|2046@zrh|朱日和|ZRC|zhurihe|zrh|2047@zrl|泽润里|ZLM|zerunli|zrl|2048@zsb" +
                "|中山北|ZGQ|zhongshanbei|zsb|2049@zsd|樟树东|ZOG|zhangshudong|zsd|2050@zsh|中山|ZSQ|zhongshan|zs|2051@zsh" +
                "|柞水|ZSY|zhashui|zs|2052@zsh|钟山|ZSZ|zhongshan|zs|2053@zsh|樟树|ZSG|zhangshu|zs|2054@ztz|张台子|ZZT" +
                "|zhangtaizi|ztz|2055@zwo|珠窝|ZOP|zhuwo|zw|2056@zwt|张维屯|ZWB|zhangweitun|zwt|2057@zwu|彰武|ZWD|zhangwu|zw" +
                "|2058@zxi|棕溪|ZOY|zongxi|zx|2059@zxi|钟祥|ZTN|zhongxiang|zx|2060@zxi|资溪|ZXS|zixi|zx|2061@zxi|镇西|ZVT" +
                "|zhenxi|zx|2062@zxi|张辛|ZIP|zhangxin|zx|2063@zxq|正镶白旗|ZXC|zhengxiangbaiqi|zxbq|2064@zya|紫阳|ZVY|ziyang" +
                "|zy|2065@zya|枣阳|ZYN|zaoyang|zy|2066@zyb|竹园坝|ZAW|zhuyuanba|zyb|2067@zye|张掖|ZYJ|zhangye|zy|2068@zyu" +
                "|镇远|ZUW|zhenyuan|zy|2069@zyx|朱杨溪|ZXW|zhuyangxi|zyx|2070@zzd|漳州东|GOS|zhangzhoudong|zzd|2071@zzh" +
                "|漳州|ZUS|zhangzhou|zz|2072@zzh|壮志|ZUX|zhuangzhi|zz|2073@zzh|子洲|ZZY|zizhou|zz|2074@zzh|中寨|ZZM" +
                "|zhongzhai|zz|2075@zzh|涿州|ZXP|zhuozhou|zz|2076@zzi|咋子|ZAL|zhazi|zz|2077@zzs|卓资山|ZZC|zhuozishan|zzs" +
                "|2078@zzx|株洲西|ZAQ|zhuzhouxi|zzx|2079@deh|东二道河|DRB|dongerdaohe|dedh|2080@fyu|抚远|FYB|fuyuan|fy|2081" +
                "@gju|革居|GEM|geju|gj|2082@gmc|光明城|IMQ|guangmingcheng|gmc|2083@hcg|寒葱沟|HKB|hanconggou|hcg|2084@hfc" +
                "|合肥北城|COH|hefeibeicheng|hfbc|2085@hhe|洪河|HPB|honghe|hh|2086@hme|虎门|IUQ|humen|hm|2087@hmn|哈密南|HLR" +
                "|haminan|hmn|2088@hnd|淮南东|HOH|huainandong|hnd|2089@lhx|漯河西|LBN|luohexi|lhx|2090@mgd|明港东|MDN" +
                "|minggangdong|mgd|2091@qfe|前锋|QFB|qianfeng|qf|2092@qsh|庆盛|QSQ|qingsheng|qs|2093@szb|深圳北|IOQ" +
                "|shenzhenbei|szb|2094@xcd|许昌东|XVF|xuchangdong|xcd|2095@xgb|孝感北|XJN|xiaoganbei|xgb|2096@xyc|西阳村|XQF" +
                "|xiyangcun|xyc|2097@xyd|信阳东|OYN|xinyangdong|xyd|2098@zmx|驻马店西|ZLN|zhumadianxi|zmdx|2099@zzd|卓资东|ZDC" +
                "|zhuozidong|zzd|2100@zzd|郑州东|ZAF|zhengzhoudong|zzd|2101";
    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        JFrame frame = new JFrame();
        frame.setTitle("Auto Completion Test");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(200, 200, 500, 400);

        ArrayList<String> items = new ArrayList<String>();
        Map map = getStationMap(StationConstant.stationString1,
                StationConstant.stationString2);
        Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            String stationName = iterator.next().toString();
            items.add(stationName);
        }

        JTextField txtInput = new JTextField();
        setupAutoComplete(txtInput, items);
        txtInput.setColumns(30);
        frame.getContentPane().setLayout(new FlowLayout());
        frame.getContentPane().add(txtInput, BorderLayout.NORTH);
        frame.setVisible(true);
    }

    private static boolean isAdjusting(JComboBox cbInput) {
        if (cbInput.getClientProperty("is_adjusting") instanceof Boolean) {
            return (Boolean) cbInput.getClientProperty("is_adjusting");
        }
        return false;
    }

    private static void setAdjusting(JComboBox cbInput, boolean adjusting) {
        cbInput.putClientProperty("is_adjusting", adjusting);
    }

    public static void setupAutoComplete(final JTextField txtInput,
                                         final ArrayList<String> items) {
        final DefaultComboBoxModel model = new DefaultComboBoxModel();
        final JComboBox cbInput = new JComboBox(model) {
            public Dimension getPreferredSize() {
                return new Dimension(super.getPreferredSize().width, 0);
            }
        };
        setAdjusting(cbInput, false);
        for (String item : items) {
            model.addElement(item);
        }
        cbInput.setSelectedItem(null);
        cbInput.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isAdjusting(cbInput)) {
                    if (cbInput.getSelectedItem() != null) {
                        txtInput.setText(cbInput.getSelectedItem().toString());
                    }
                }
            }
        });

        txtInput.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                setAdjusting(cbInput, true);
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (cbInput.isPopupVisible()) {
                        e.setKeyCode(KeyEvent.VK_ENTER);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ENTER
                        || e.getKeyCode() == KeyEvent.VK_UP
                        || e.getKeyCode() == KeyEvent.VK_DOWN) {
                    e.setSource(cbInput);
                    cbInput.dispatchEvent(e);
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        txtInput.setText(cbInput.getSelectedItem().toString());
                        cbInput.setPopupVisible(false);
                    }
                }
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    cbInput.setPopupVisible(false);
                }
                setAdjusting(cbInput, false);
            }
        });
        txtInput.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateList();
            }

            public void removeUpdate(DocumentEvent e) {
                updateList();
            }

            public void changedUpdate(DocumentEvent e) {
                updateList();
            }

            private void updateList() {
                setAdjusting(cbInput, true);
                model.removeAllElements();
                String input = txtInput.getText();
                if (!input.isEmpty()) {
                    for (String item : items) {
                        if (item.toLowerCase().startsWith(input.toLowerCase())) {
                            model.addElement(item);
                        }
                    }
                }
                cbInput.setPopupVisible(model.getSize() > 0);
                setAdjusting(cbInput, false);
            }
        });
        txtInput.setLayout(new BorderLayout());
        txtInput.add(cbInput, BorderLayout.SOUTH);
    }

    /**
     * 获取站点所对应的站点名
     *
     * @param stations1 站点的字符串一
     * @param stations2 站点的字符串二 因为一个字符串装不下
     */
    public static Map getStationMap(String stations1, String stations2) {
        Map map = new HashMap();
        if (!stations1.equals(null)) {
            String[] strs1 = stations1.split("@");
            for (int i = 1; i < strs1.length; i++) {
                String[] strs2 = strs1[i].split("\\|");
                for (int j = 0; j < strs2.length; j++) {
                    map.put(strs2[1], strs2[2]);
                }
            }
        }
        if (!stations2.equals(null)) {
            String[] strs2 = stations2.split("@");
            for (int i = 1; i < strs2.length; i++) {
                String[] strs3 = strs2[i].split("\\|");
                for (int j = 0; j < strs3.length; j++) {
                    map.put(strs3[1], strs3[2]);
                }
            }
        }
        return map;
    }
}
