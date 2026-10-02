package com.example.novel.config;

import com.example.novel.entity.Chapter;
import com.example.novel.entity.Novel;
import com.example.novel.entity.User;
import com.example.novel.repository.ChapterRepository;
import com.example.novel.repository.NovelRepository;
import com.example.novel.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final NovelRepository novelRepository;
    private final ChapterRepository chapterRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           NovelRepository novelRepository,
                           ChapterRepository chapterRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.novelRepository = novelRepository;
        this.chapterRepository = chapterRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("admin")) {
            log.info("DataInitializer: 检测到已有数据,跳过初始化。");
            return;
        }
        log.info("DataInitializer: 开始初始化示例数据...");

        // 1. 用户
        User admin = new User("admin",
                passwordEncoder.encode("123456"),
                "ADMIN");
        User test = new User("test",
                passwordEncoder.encode("123456"),
                "USER");
        userRepository.save(admin);
        userRepository.save(test);
        log.info("  ✓ 用户: admin / test (密码均为 123456, 已 BCrypt 加密)");

        // 2. 小说 + 章节
        Novel n1 = new Novel(
                "九天神帝",
                "墨羽长歌",
                "https://placehold.co/200x280/2c3e50/ffffff?text=九天神帝",
                "少年林尘自废墟中崛起,执掌天帝传承,踏上一条逆天而行的浩瀚征途。九重天阙,万古仙朝,且看他如何以一柄断剑,劈开苍穹,成就无上神帝之名。",
                "玄幻",
                "连载中");
        addChapters(n1, List.of(
                new Chapter("第一章 废墟少年",
                        "       荒凉的北荒之地,天色阴沉,狂风卷着黄沙呼啸而过。\n\n" +
                                "       林尘自黑暗中睁开双眼,浑身骨骼仿佛散架了一般,钻心的痛楚让他几乎再度昏厥过去。" +
                                "他挣扎着爬起身,环顾四周——这是一片破败的神庙遗迹,断壁残垣之上刻满了斑驳的古老符文。\n\n" +
                                "       \"我……竟然没死。\"\n\n" +
                                "       三日前,林家满门被屠,唯有他从血泊中爬出,跌落山崖。脑海中不断回荡着那夜火光冲天的画面,父亲将他推入地道时的最后一眼,至今仍灼烧着他的心。\n\n" +
                                "       他低头,看见自己胸口隐隐浮现一枚金色的印记——那是传说中早已断绝的天帝传承。\n\n" +
                                "       \"既然天不收我,那便由我来覆灭这漫天诸神。\"少年的声音沙哑却坚定,寒风里,那柄插在废墟中央的断剑骤然嗡鸣。", 1),
                new Chapter("第二章 天帝印记",
                        "       断剑出鞘的瞬间,一股浩瀚的意志如同潮水般涌入林尘的识海。\n\n" +
                                "       无数古老的画面在他眼前飞速掠过——有俯瞰万族的天帝、有破碎的星河、有被血染红的大地。那是上一纪元的辉煌与毁灭,是九重天阙最终的结局。\n\n" +
                                "       \"吾以吾血,封尔道基。汝承吾志,当开太平。\"\n\n" +
                                "       一道苍老的声音在他脑海中回响,旋即那枚金色印记剧烈灼烧,一股温热的暖流自胸口蔓延全身,所过之处,断裂的经脉竟在缓缓愈合。\n\n" +
                                "       林尘猛地睁开眼,只觉体内灵力暴涨数倍,原本被废的丹田此刻金光熠熠。\n\n" +
                                "       \"这便是……天帝传承么?\"\n\n" +
                                "       他缓缓握紧那柄断剑,剑身之上古老铭文浮现,发出低沉的龙吟之声。远处,似有脚步声急促而来,林尘眼神骤然冰冷,隐入残垣之后。", 2),
                new Chapter("第三章 林家余孽",
                        "       \"搜!给我仔细搜!那小子中了少爷的噬心钉,绝不可能逃远!\"\n\n" +
                                "       数十名黑衣武者如同猎犬般在废墟中散开,刀光在夜色中闪烁。为首者正是当日率人屠灭林家的王家家将——王厉。\n\n" +
                                "       林尘隐于暗处,目光如刀。三日前他还不过是凝气境的小修士,而眼前这王厉,却是实打实的化海境强者。\n\n" +
                                "       \"差距太大,不能硬碰。\"\n\n" +
                                "       他悄然运转天帝传承中记载的\"龟息诀\",气息瞬间收敛至近乎虚无。就在王厉从他藏身的断壁前走过时,胸口的天帝印记微微一闪,一股金色剑气激射而出——\n\n" +
                                "       \"噗!\"\n\n" +
                                "       王厉瞳孔骤缩,低头看着胸口凭空出现的血洞,至死都没明白自己究竟是如何被一名废人所杀。其余武者大惊失色,纷纷后退,林尘却已借着夜色,消失于茫茫荒原。\n\n" +
                                "       \"王家……这只是开始。\"\n\n" +
                                "       少年的声音被风沙吞没,九天神帝之路,从今夜正式开启。", 3)
        ));
        n1.setViewCount(152L);
        novelRepository.save(n1);

        Novel n2 = new Novel(
                "都市巅峰",
                "老狐点灯",
                "https://placehold.co/200x280/c0392b/ffffff?text=都市巅峰",
                "外卖小哥陈风意外撞见豪门千金被陷害一幕,从此卷入一场跨越商界、地下世界与顶级权贵的惊天棋局。且看草根如何一路逆袭,登顶都市之巅。",
                "都市",
                "连载中");
        addChapters(n2, List.of(
                new Chapter("第一章 撞破阴谋",
                        "       深城,夜里十一点,雨还在下。\n\n" +
                                "       陈风骑着电动车拐进滨江一号别墅区,正准备送完这单收工,却被眼前一幕惊得浑身僵硬——\n\n" +
                                "       别墅车库门口,一名女子被两个黑衣保镖架住,正拼命挣扎。她面色苍白,眼中满是惊恐,手腕上的翡翠镯子在昏暗灯光下绿得刺眼。\n\n" +
                                "       \"大小姐,老爷说了,今晚这股权转让书,您必须签。\"\n\n" +
                                "       \"我父亲绝不会害我!你们……你们到底是谁?!\"\n\n" +
                                "       陈风下意识摸出手机,却被其中一名保镖瞥见。对方眼神骤冷,径直朝他走来。\n\n" +
                                "       \"小子,看到了不该看的……\"\n\n" +
                                "       话音未落,陈风猛拧电门,电动车蹿出去的同时,他大喊一声\"救命啊,杀人了!\",整个别墅区顿时灯火通明。\n\n" +
                                "       保镖脸色剧变,架着女子迅速退入车中,一辆黑色商务车咆哮着消失在雨夜。陈风喘着粗气,心跳如擂鼓,他不知道,自己这一嗓子,竟意外救下了深城首富的独女。", 1),
                new Chapter("第二章 豪门邀请",
                        "       三天后,陈风被请进深城最顶级的写字楼——盛世集团总部。\n\n" +
                                "       一名西装革履的秘书将他领进董事长办公室,那位他曾在电视上无数次见过的传奇女人,此刻正坐在宽大的办公桌后,目光深邃地打量着他。\n\n" +
                                "       \"你就是那天晚上喊救命的小哥?\"\n\n" +
                                "       \"是……是的,沈总。\"陈风紧张得声音发颤。\n\n" +
                                "       \"坐下。\"沈慕云合上文件,\"你救了我女儿一命,这份恩情,我沈家记下了。我可以给你一千万,也可以给你一份体面的工作。\n\n" +
                                "       你选哪个?\"\n\n" +
                                "       陈风愣了好一会儿,脑海中闪过送外卖这三年来的风吹日晒、父母的病榻、女友的离去。他缓缓抬起头,眼中第一次露出一丝锋芒。\n\n" +
                                "       \"沈总,钱我不要。我要的是……一个机会。\"\n\n" +
                                "       沈慕云嘴角微微扬起,轻声说道:\"有胆识。那从明天起,你就是盛世集团最年轻的……投资部副总监。\"", 2),
                new Chapter("第三章 初露锋芒",
                        "       投资部晨会上,陈风被一沓厚厚的资料砸在脸上。\n\n" +
                                "       \"副总监?呵,一个送外卖的也配坐这位置?\"投资部总监赵铭冷笑,\"沈总给你脸,你还真敢接。今天下班前,把这份并购案的可行性报告交出来,做不到,自己滚。\"\n\n" +
                                "       全场鸦雀无声,所有人都在等着看这个\"空降兵\"的笑话。\n\n" +
                                "       陈风没有发火,只是平静地接过资料,走回自己的工位。他熬了整整一夜,把整份并购案拆解得清清楚楚——目标公司估值虚高、对赌协议存在致命陷阱、对方资金链濒临断裂。\n\n" +
                                "       第二天清晨,当赵铭踩着点走进办公室,陈风已经将一份 38 页的深度报告摆在他桌上,末尾写着醒目的红色大字: \"建议:拒绝并购,反向做空目标公司。\"\n\n" +
                                "       赵铭的手微微发抖。他猛然意识到,这个年轻人不是来混日子的——他是来掀桌子的。\n\n" +
                                "       都市的牌局,从此刻起,彻底失控。", 3)
        ));
        n2.setViewCount(97L);
        novelRepository.save(n2);

        Novel n3 = new Novel(
                "仙路漫漫",
                "云中鹤影",
                "https://placehold.co/200x280/16a085/ffffff?text=仙路漫漫",
                "资质平平的少年苏辰偶得一枚来历神秘的玉简,从此踏入浩瀚修真界。漫漫仙路,荆棘满布,挚爱、仇敌、机缘、劫难交织,且看他如何以凡人之心,问鼎苍穹。",
                "修真",
                "已完结");
        addChapters(n3, List.of(
                new Chapter("第一章 玉简惊变",
                        "       落霞山,黄昏。\n\n" +
                                "       苏辰背着竹篓从山中采药归来,一脚踩空,跌入一处被荒草掩盖的山洞。洞底淤泥中,一枚温润的玉简静静躺着,仿佛等待了他千年。\n\n" +
                                "       他颤抖着将玉简拾起,指尖刚一触碰,一股浩瀚的信息流瞬间涌入识海——\n\n" +
                                "       \"吾乃青玄真人,渡劫失败,仅余一缕残识藏于此简。汝若与吾有缘,当承吾道统,行吾未竟之路……\"\n\n" +
                                "       苏辰头痛欲裂,却咬牙硬撑。半炷香后,他缓缓睁开眼,眼中已多出一卷名为《太上逍遥诀》的无上功法。\n\n" +
                                "       \"修真……我苏辰,也能修真了么?\"\n\n" +
                                "       他攥紧玉简,抬头望向洞口那一线暮色天光。少年眼中,有星火燎原。", 1),
                new Chapter("第二章 初入宗门",
                        "       一年后,苏辰凭《太上逍遥诀》筑基成功,被途经的玄天宗长老发现根骨惊奇,破例收入内门。\n\n" +
                                "       \"什么?就他?一个散修也想进内门?\"宗门大殿之上,众弟子议论纷纷,大师兄李玄更是满脸不屑。\n\n" +
                                "       \"苏辰,你可知内门规矩?\"长老沉声问道。\n\n" +
                                "       \"弟子知晓。\"苏辰平静答道,\"内门之中,强者为尊。\"\n\n" +
                                "       \"好。\"长老点头,\"三月之后的宗门大比你若能进入前十,便正式入内。若进不了……就回你那落霞山继续种田吧。\"\n\n" +
                                "       李玄冷笑一声,甩袖而去。所有人都以为苏辰不过是个笑话,却不知这位\"笑话\",早已在洞府中闭关修炼,将《太上逍遥诀》第三层彻底参透。\n\n" +
                                "       三月之期,一战成名。", 2),
                new Chapter("第三章 漫漫仙路",
                        "       宗门大比之上,苏辰连败九名内门弟子,最终与大师兄李玄决战紫霄台。\n\n" +
                                "       两人交锋数百回合,李玄渐渐露出疲态,而苏辰的眼中却始终平静如水。\n\n" +
                                "       \"你……你究竟是什么怪物?!\"李玄嘶吼。\n\n" +
                                "       \"我不是怪物。\"苏辰轻声答道,\"我只是一个想要守护一些人、改变一些事的人。\"\n\n" +
                                "       话音落下,他一指点出,《太上逍遥诀》运转至极,漫天剑光化作一道青色长虹,贯穿李玄胸口。\n\n" +
                                "       那一日,玄天宗上下震动;那一日,苏辰之名传遍修真界。\n\n" +
                                "       而对他而言,这不过是漫漫仙路上,极小的一步。前方,还有渡劫、大乘、飞升……还有那高高在上的仙界,等他去叩问。\n\n" +
                                "       \"仙路漫漫,我自逍遥。\"\n\n" +
                                "       少年的身影踏剑而起,直冲云霄,留下一道青色长虹,久久不散。", 3)
        ));
        n3.setViewCount(213L);
        novelRepository.save(n3);

        long novelTotal = novelRepository.count();
        long chapterTotal = chapterRepository.count();
        long userTotal = userRepository.count();
        log.info("DataInitializer: 完成 ✓  共 {} 用户 / {} 小说 / {} 章节",
                userTotal, novelTotal, chapterTotal);
    }

    private void addChapters(Novel novel, List<Chapter> chapters) {
        for (Chapter c : chapters) {
            novel.addChapter(c);
        }
    }
}