/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.livehereandnow.ages.engine;

//import com.livehereandnow.ages.card.Card;
import com.livehereandnow.ages.card.AgesCard;
import com.livehereandnow.ages.card.AgesCardFactory;
import com.livehereandnow.ages.card.AgesCommon;
import static com.livehereandnow.ages.card.AgesCommon.STYLE_實驗室;
import static com.livehereandnow.ages.card.AgesCommon.STYLE_政府區;
import static com.livehereandnow.ages.card.AgesCommon.STYLE_普通;
import static com.livehereandnow.ages.card.AgesCommon.STYLE_普通_藍點;
import static com.livehereandnow.ages.card.AgesCommon.STYLE_領袖區;
import com.livehereandnow.ages.card.DoneDescription;
import com.livehereandnow.ages.card.ReasonWhyNoAction;
import com.livehereandnow.ages.exception.AgesException;
import com.livehereandnow.ages.field.Points;
import com.livehereandnow.ages.field.Score;
import com.livehereandnow.ages.field.Token;
import com.livehereandnow.ages.server.AgesGameServerJDBC;
import com.livehereandnow.ages.ui.Sector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

/**
 *
 * @author mark
 */
public class Ages implements AgesCommon {

    // constants
//    final String[] STAGE_NAME = {" ", "政治階段", "內政階段"};
    // variables
    private AgesCard NOCARD_HERE = new AgesCardFactory().getNOCARD();
    private Player p1;
    private Player p2;
    private Player currentPlayer;
    private Player 當前操作玩家;
    private List<Player> allPlayers;
    AgesCardFactory cardFactory;
    private List<AgesCard> allCards;
    private List<AgesCard> qryCards;
    private List<AgesCard> 卡牌列;
    private List<AgesCard> 時代A內政牌;
    private List<AgesCard> 時代I內政牌;
    private List<AgesCard> 時代II內政牌;
    private List<AgesCard> 時代III內政牌;
    private List<AgesCard> 時代A軍事牌;
    private List<AgesCard> 時代I軍事牌;
    private List<AgesCard> 時代II軍事牌;
    private List<AgesCard> 時代III軍事牌;
    private List<AgesCard> 軍事牌回收區;
    private List<AgesCard> 未來事件;
    private List<AgesCard> 當前事件;
    private List<AgesCard> 現在發生事件;

    public boolean parser(String cmd) throws IOException, AgesException {
        //
        // 1. init
        //
        int tokenCnt = 0;//命令行裡共有幾個字，給予初值為0
        String keyword = "";//指令是什麼，給予初值空字符串
        int p1 = -1;//指令的參數是什麼，給予初值為-1，-1通常是指不能用的值
        int p2 = -1;
        int p3 = -1;

        //
        // 2. parser to words 
        //
        //將命令行的句子拆解為字，以空格格開為依據，空格都不記
        String[] strTokens = cmd.split(" ");
        List<String> tokens = new ArrayList<>();
        for (String item : strTokens) {
            if (item.length() > 0) {
                tokens.add(item);
            }
        }
        tokenCnt = tokens.size();//賦予變量tokenCnt真正的值，真正的值是指到底打個幾個字

        //
        // 3. to execute command based on size
        //
        if (tokenCnt == 0) {//when simple enter
            return true; // silently ignore it
        }
        // 
        keyword = tokens.get(0);//指令的關鍵字是第0個字，例如take 3的take

        if (tokenCnt == 1) {//如果輸入的是一個字的話

            try {
                int id = Integer.parseInt(keyword);
                return doCmd(id);
            } catch (Exception ex) {
                doCmd(keyword);
                return true;
            }

        }
        if (tokenCnt == 2) {//如果輸入的是2個字的話
            try {
                p1 = Integer.parseInt(tokens.get(1));
            } catch (Exception ex) {
                System.out.println("Parameter must be integer!");
                return false;
            }
            doCmd(keyword, p1);
            return true;
        }

        if (tokenCnt == 3) {//如果輸入的是2個字的話
            try {
                p1 = Integer.parseInt(tokens.get(1));
                p2 = Integer.parseInt(tokens.get(2));
            } catch (Exception ex) {
                System.out.println("Parameter must be integer!");
                return false;
            }
            return doCmd(keyword, p1, p2);
        }

        // ver 0.62 for upgrad 3 0 1, Upgrad Farm from Age A to Age I
        if (tokenCnt == 4) {//如果輸入的是3個字的話
            try {
                p1 = Integer.parseInt(tokens.get(1));
                p2 = Integer.parseInt(tokens.get(2));
                p3 = Integer.parseInt(tokens.get(3));
            } catch (Exception ex) {
                System.out.println("Parameter must be integer!");
                return false;
            }
            return doCmd(keyword, p1, p2, p3);
        }

        //
        System.out.println("Cureently command must be one or two words only!");
        return false;

    }

    public static void main(String[] args) throws AgesException, IOException {
        Ages ages = new Ages();
        InputStreamReader cin = new InputStreamReader(System.in);
        BufferedReader in = new BufferedReader(cin);
//        engine.getField().showSector();
        StringBuilder sb;
        while (true) {
            sb = new StringBuilder();
            sb.append(ages.get當前操作玩家().getName()).append("@");
            sb.append("回合");
            sb.append(ages.get當前回合());
            sb.append(ages.getCurrentPlayerName());
            sb.append(STAGE_NAME[ages.get現在階段()]);
            System.out.print(sb.toString() + "$ ");

//            System.out.print("" + ages.getCurrentPlayerName() + " >> ");
            ages.parser(in.readLine());
//            engine.doCmd("9999");

        }
    }

    public Player get當前操作玩家() {
        return 當前操作玩家;
    }

    public void set當前操作玩家(Player 當前操作玩家) {
        this.當前操作玩家 = 當前操作玩家;
//        show時代回合玩家階段();
    }

    public void 交換玩家() {

        if (currentPlayer == p1) {
            currentPlayer = p2;
            當前操作玩家 = currentPlayer;
//            show時代回合玩家階段();

            return;
        }
        if (currentPlayer == p2) {
            currentPlayer = p1;
//            System.out.println("auto to next 回合");
            round.addPoints(1);

            當前操作玩家 = currentPlayer;
//            show時代回合玩家階段();

            return;
        }

    }
    private Points round;
    private int 當前時代;
    private int 現在階段;
    public final int 政治階段 = 1;
    public final int 內政階段 = 2;

    public int get當前回合() {
        return round.getVal();
    }

    public Points getRound() {
        return round;
    }

    public void setRound(Points round) {
        this.round = round;
    }

    public int get現在階段() {
        return 現在階段;
    }

    public void sub從政治階段轉換到內政階段() {
        this.set現在階段(內政階段);
    }

    public void set現在階段(int 現在階段) {
        this.現在階段 = 現在階段;
    }

    public int get當前時代() {
        return 當前時代;
    }

    public void set當前時代(int 當前時代) {
        this.當前時代 = 當前時代;
    }

    public void reset() {
        //
        round = new Points();
        round.setVal(0);
        //
        p1 = new Player("AAA");
        p2 = new Player("BBB");
        allPlayers = new ArrayList<>();
        allPlayers.add(p1);
        allPlayers.add(p2);
        currentPlayer = p1;
        當前操作玩家 = p1;
        //
        cardFactory = new AgesCardFactory();
        allCards = cardFactory.getEntireList();
        qryCards = cardFactory.getEntireList();

        //
        卡牌列 = new ArrayList<>();
        未來事件 = new ArrayList<>();
        當前事件 = new ArrayList<>();
        現在發生事件 = new ArrayList<>();
        軍事牌回收區 = new ArrayList<>();

        //
        //
        //
        時代A內政牌 = cardFactory.getAgeCivil(0);
        時代I內政牌 = cardFactory.getAgeCivil(1);
        時代II內政牌 = cardFactory.getAgeCivil(2);
        時代III內政牌 = cardFactory.getAgeCivil(3);
        時代A軍事牌 = cardFactory.getAgeMilitary(0);
        時代I軍事牌 = cardFactory.getAgeMilitary(1);
        時代II軍事牌 = cardFactory.getAgeMilitary(2);
        時代III軍事牌 = cardFactory.getAgeMilitary(3);
//        軍事牌回收區=;

        //
        // Shuffle
        //
        Collections.shuffle(時代A內政牌);
        Collections.shuffle(時代A軍事牌);
        Collections.shuffle(時代I內政牌);
        Collections.shuffle(時代I軍事牌);
        Collections.shuffle(時代II內政牌);
        Collections.shuffle(時代II軍事牌);
        Collections.shuffle(時代III內政牌);
        Collections.shuffle(時代III軍事牌);
//        Collections.shuffle(軍事牌回收區);

        //
        // basic 6 cards for each player
        //
        for (Player player : allPlayers) {
            player.get政府區().add(cardFactory.getCardByName("專制"));
            player.get實驗室().add(cardFactory.getCardByName("哲學"));
            player.get神廟區().add(cardFactory.getCardByName("宗教"));
            player.get農場區().add(cardFactory.getCardByName("農業"));
            player.get礦山區().add(cardFactory.getCardByName("青銅"));
            player.get步兵區().add(cardFactory.getCardByName("戰士"));

            player.get步兵區().get(0).setTokenYellow(1);
            player.get實驗室().get(0).setTokenYellow(1);
            player.get礦山區().get(0).setTokenYellow(2);
            player.get農場區().get(0).setTokenYellow(2);
            player.get人力庫_黃點().setVal(18);
            player.get工人區_黃點().setVal(1);
            player.get資源庫_藍點().setVal(18);

            //
            player.subUpdate手牌上限();
        }

    }

    public void 交換當前操作玩家() {
        if (當前操作玩家 == p1) {
            當前操作玩家 = p2;
            return;
        }
        if (當前操作玩家 == p2) {
            當前操作玩家 = p1;
            return;
        }
    }

    /**
     * 5/15 17:15, by Mark
     */
    public void produce() {
        currentPlayer.produce();
    }

    public String getCurrentPlayerName() {
        return currentPlayer.getName();
    }

//    public void setCurrentPlayer(Player currentPlayer) {
//        this.currentPlayer = currentPlayer;
//    }
    public Player getP1() {
        return p1;
    }

    public Player getP2() {
        return p2;
    }

    public List<Player> getAllPlayers() {
        return allPlayers;
    }

    private AgesGameServerJDBC server;
//    private EngineCore core;
    private Field field;

    public Field getField() {
        return field;
    }

    public void setField(Field field) {
        this.field = field;
    }
//   private Player player;
//    private AgesCard card;
    private String debug;

    public Player getPlayer(int player) {
        switch (player) {
            case 1:
                return p1;
            case 2:
                return p2;
            default:
                return new Player("UNKNOWN");

        }
    }

    public List<AgesCard> getPlayerSector(int player, int sector) {
        return getPlayer(player).getSector(sector);
    }

    public String getDebug() {
        return debug;
    }

    public void setDebgug(String str) {
        debug = str;
    }

    /**
     *
     * @param from source sector
     * @param index index of
     * @param to target sector
     */
    public void moveOneCard(List<AgesCard> from, int index, List<AgesCard> to) {

        to.add(from.remove(index));
    }

    public AgesCard getNOCARD() {
        return field.getNOCARD();

    }

    public AgesCard getCardFromCardRow(int index) {

        try {
            return field.getCardRow().get(index);
        } catch (Exception ex) {
            return field.getNOCARD();
        }
    }

    public Ages() throws AgesException {
        server = new AgesGameServerJDBC();
        reset();
        init();

    }

    public void show時代回合玩家階段() {
            // draft style, quick to showSector
        // System.out.println("時代" + FULLWIDTH_COLON + AGE_NAME[ 當前時代] + "\u3000\uFF21回合:" + round.getVal() + " 玩家:" + currentPlayer.getName() + " 階段(政治/內政):" + STAGE_NAME[現在階段]);

        // better style, easy to maintain
        StringBuilder sb = new StringBuilder();
        sb.append(FULLWIDTH_EQ_SIGN).append(FULLWIDTH_EQ_SIGN).append(FULLWIDTH_EQ_SIGN);
        sb.append(FULLWIDTH_SPACE).append("時代").append(FULLWIDTH_COLON).append(AGE_NAME[ 當前時代]);
        sb.append(FULLWIDTH_SPACE).append("回合").append(FULLWIDTH_COLON).append(round.getVal());
        sb.append(FULLWIDTH_SPACE).append("玩家").append(FULLWIDTH_COLON).append(currentPlayer.getName());
        sb.append(FULLWIDTH_SPACE).append("階段").append(FULLWIDTH_COLON).append(STAGE_NAME[現在階段]);
        sb.append(FULLWIDTH_SPACE).append("當前操作玩家").append(FULLWIDTH_COLON).append(get當前操作玩家().getName());
        sb.append(FULLWIDTH_SPACE);

        sb.append(FULLWIDTH_EQ_SIGN).append(FULLWIDTH_EQ_SIGN).append(FULLWIDTH_EQ_SIGN);
        System.out.println(sb.toString());
    }

    private void init() {
        field = new Field();
        reset();
    }

    String returnStr = " return str...";

    public String getFeedback() {
//        return core.getCardRowInfo();
//        return core.getFeedback();
//        return returnStr;
        return "...";
    }

    public void setFeedback(String str) {
        returnStr = str;
    }

    public boolean xxxparser(String cmd) throws IOException, AgesException {
        setDebgug("...start to parse " + cmd);
        //
        // 1. init
        //
        int tokenCnt = 0;//命令行裡共有幾個字，給予初值為0
        String keyword = "";//指令是什麼，給予初值空字符串
        int p1 = -1;//指令的參數是什麼，給予初值為-1，-1通常是指不能用的值
        int p2 = -1;
        int p3 = -1;

        //
        // 2. parser to words 
        //
        //將命令行的句子拆解為字，以空格格開為依據，空格都不記
        String[] strTokens = cmd.split(" ");
        List<String> tokens = new ArrayList<>();
        for (String item : strTokens) {
            if (item.length() > 0) {
                tokens.add(item);
            }
        }
        tokenCnt = tokens.size();//賦予變量tokenCnt真正的值，真正的值是指到底打個幾個字

        //
        // 3. to execute command based on size
        //
        if (tokenCnt == 0) {//when simple enter
            return true; // silently ignore it
        }
        // 
        keyword = tokens.get(0);//指令的關鍵字是第0個字，例如take 3的take

        if (tokenCnt == 1) {//如果輸入的是一個字的話
            String feedback = doCmd(keyword);
            setDebgug(feedback);

            System.out.println("123456789");
            getP1().sub更新文明板塊上所提供的數據();
            getP2().sub更新文明板塊上所提供的數據();

            return true;
        }
        if (tokenCnt == 2) {//如果輸入的是2個字的話
            try {
                p1 = Integer.parseInt(tokens.get(1));
            } catch (Exception ex) {
                System.out.println("Parameter must be integer!");
                return false;
            }
            return doCmd(keyword, p1);
        }

        if (tokenCnt == 3) {//如果輸入的是2個字的話
            try {
                p1 = Integer.parseInt(tokens.get(1));
                p2 = Integer.parseInt(tokens.get(2));
            } catch (Exception ex) {
                System.out.println("Parameter must be integer!");
                return false;
            }
            return doCmd(keyword, p1, p2);
        }

        // ver 0.62 for upgrad 3 0 1, Upgrad Farm from Age A to Age I
        if (tokenCnt == 4) {//如果輸入的是3個字的話
            try {
                p1 = Integer.parseInt(tokens.get(1));
                p2 = Integer.parseInt(tokens.get(2));
                p3 = Integer.parseInt(tokens.get(3));
            } catch (Exception ex) {
                System.out.println("Parameter must be integer!");
                return false;
            }

            return doCmd(keyword, p1, p2, p3);
        }

        //
//        System.out.println("Cureently command must be one or two words only!");
//        setFeedback("   unknown command," + cmd + ", just ignore it!");
//        setFeedback();
        setDebgug("[parser]: unknown command," + cmd + ", just ignore it!");
        System.out.println("123456789");
        getP1().sub更新文明板塊上所提供的數據();
        getP2().sub更新文明板塊上所提供的數據();
        return false;

    }

    public boolean doCmd(int id) throws IOException, AgesException {
//        return field.
        if (id == 0) {
            field.show卡牌列();
            field.show軍事牌回收區();
            return true;
        }
        if (id == 1) {
            p1.show();
            return true;
        }
        if (id == 2) {
            p2.show();
            return true;
        }

        if (id > 1000 && id < 9999) {
            field.showCardInfo(id);
        }

        return true;
    }

    public String doCmd(String keyword) throws IOException, AgesException {
        switch (keyword) {
            case "00":
                field.show卡牌列();
                field.show軍事牌回收區();
                return "00";
            case "9999":
                getP1().sub更新文明板塊上所提供的數據();
                getP2().sub更新文明板塊上所提供的數據();
                return "9999";
            case "server":

                return doServer();
            case "new-game":
                return doNewGame();
            case "p"://工人區_黃點 
                return doPopulation();

            case "act":
                return act執行行動牌();
            case "list":
                return doList();
            case "start":
                return doStart();
            case "increase-population"://v0.52
            case "population"://v0.52
//                return core.doIncreasePopulation();
            case "revolution"://v0.39
//                return core.doRevolution();
            case "govt"://v0.39
            case "change-government"://v0.39
//                return core.doChangeGovernment();

            case "construct-wonder":
            case "wonder":
            case "w":
//                return core.doConstructWonder();

            case "help":
//                return core.doHelp();
            case "h":
//                return core.doHelpShort();
            case "s":
            case "status":
//                return core.doStatus();
//                field.showSector(0);
//                showSector(0);
                show時代回合玩家階段();
                field.show卡牌列();
                field.show軍事牌回收區();
                p1.show();
                p2.show();
                return " just did field.show(0)";
            case "ss":
//                showSector(10);
                show時代回合玩家階段();
                return " just did field.show(10)";
            case "version":
                return doVersion();
            case "shuffle":
                return shuffle();
            case "change-turn":
            case "c":
            case "":
                return doChangeTurn();
            case "cc":

                doChangeTurnV2();
                return " just did doChangeTurnV2()";
            case "change-stage":
            case "cs":
                doChangeStage();
                return " just did doChangeStage()";
            case "to":
            case "tt":

                return do拿牌打牌測試用();

            default:
                System.out.println("Unknown keyword, " + keyword);
                return "Unknown keyword, " + keyword;
        }

    }

    public boolean doCmd(String keyword, int val) throws IOException, AgesException {
        switch (keyword) {
            case "status":
            case "s":
//                showSector(val)
                return true;
            case "list":
                return doList(val);

            case "b":
            case "build":
//                 if (this.get當前回合() == 1) {
//                    System.out.println("按遊戲規則:第一回合，只能拿牌，不能做建造");
//                    return false;
//                }

                return act建造(val);
//                return true;

            case "act":
                return actAct(val);
            case "打":
            case "p":
            case "o":
            case "out":

            case "play":
            case "play-card":
            case "out-card":
                if (this.get當前回合() == 1) {
                    System.out.println("按遊戲規則:第一回合，只能拿牌，不能做打牌");
                    return false;
                }
                return act打內政牌(val);
            case "om"://out-military 
                return act丟棄軍事牌(val);
            case "military":
            case "m":
                return act打軍事牌(val);
            case "oo":
//                return core.doPlayCard革命(val);
            case "拿"://在我的環境NetBeans無法執行，但是在DOS可以
            case "拿牌":
            case "t":
            case "take":
            case "take-card":
                return act拿牌(val);

            case "destroy":
            case "d":
                if (this.get當前回合() == 1) {
                    System.out.println("按遊戲規則:第一回合，只能拿牌，不能做摧毀");
                    return false;
                }
                actDestroy(val);
                return true;

            case "doEvent":
            case "e":
                return doEvent(val);
            default:
                System.out.println("Unknown keyword, " + keyword);
                return false;
        }
    }

    public boolean doCmd(String keyword, int p1, int p2) throws IOException, AgesException {
        switch (keyword) {
            case "upgrade":
            case "u":
                return act升級(p1, p2);

            default:
                System.out.println("Unknown keyword, " + keyword);
                return false;
        }
    }

    public boolean doCmd(String keyword, int p1, int p2, int p3) throws IOException, AgesException {
        switch (keyword) {
            case "upgrade":
            case "u":
//                return core.doUpgrade(p1, p2, p3);

            default:
                System.out.println("Unknown keyword, " + keyword);
                return false;
        }
    }

    public String doVersion() {
        //  System.out.println(" TODO   [A內政-亞歷山大圖書館 科技生產+1，文化生產+1，內政手牌上限+1，軍事手牌上限+1]  ");
        //getBuildingLimit()

        System.out.println("  === ver 0.5 ===  2014-5-12, 12:32, by Max　");
        System.out.println("    1.處理回合數內的生產");

        System.out.println("  === ver 0.4 ===  2014-5-12, 12:32, by Max　");
        System.out.println("    1.要處理回合數");

        System.out.println("  === ver 0.3 ===  2014-5-12, 11:45, by Max　");
        System.out.println("    1.準備作交換玩家");
        System.out.println("  === ver 0.2 ===  2014-5-12, 10:05, by Max　");
        System.out.println("    Done 需要增加setCurrentPlayer");
        System.out.println("    1.建立騎兵區、炮兵區、空軍區、劇院區、圖書館區、競技場區");
        System.out.println("  === ver 0.1 ===  2014-5-10, 16:47, by Max　");
        System.out.println("    1. 建立遊戲引擎的變量，能夠運作的地方");

        return " just did doVersion";
    }

    private String doNewGame() {
        reset();
        return "New Game is set!";
    }

    /**
     * play card from 手牌內政牌區 by index
     * <br> Not allow to play non-existing card
     *
     * @param index
     * @return
     */
    private boolean act打內政牌(int index) {
        // 1
        if (index > currentPlayer.get手牌內政牌區().size() - 1 || index < 0) {
            System.out.println("我無法作出這個動作，我這個位置沒有牌");
            return false;
        }
        // 2
        if (getCurrentPlayerRemainingCivilPoints() < 1) {
            System.out.println("我無法作出這個動作，NO 內政點數 FOR THIS act打牌 ");
            return false;
        }

        // 3     
        AgesCard card = currentPlayer.手牌內政牌區.get(index);
//        System.out.println(""+card.getCostRevolution());
        if (card.is科技牌()) {
            if (currentPlayer.get科技().getVal() < card.getCostIdea()) {
                System.out.println("打出這張牌需要花費(" + card.getCostIdea() + ")科技");
                System.out.println("我無法作出這個動作，NOT ENOUGH 科技 FOR THIS act打牌 ");

                return false;
            }
        }
// 4     
        //    AgesCard card = currentPlayer.手牌內政牌區.get(index);
//        System.out.println(""+card.getCostRevolution());
        if (card.is行動牌()) {
            System.out.println("card.is行動牌() "
                    + " card.get拿牌回合() is " + card.get拿牌回合()
                    + " get當前回合().getVal() is " + get當前回合());

            if (card.get拿牌回合() >= get當前回合()) {
                System.out.println("YOU CANNOT TAKE AND PLAY GIVEN 行動牌 WITHIN SAME ROUND!");
                return false;
            }

        }

        act打內政牌core(index);
        return true;
    }

    private boolean act打內政牌core(int index) {
        currentPlayer.actPlayCivilCard(index);
        return true;
    }

    private boolean act打軍事牌core(int index) {
        currentPlayer.act玩家丟棄軍事牌(index);
        return true;
    }

    public boolean actTakeCardBySeq(int seq) throws AgesException {
        AgesCard ac;
        for (int k = 0; k < 13; k++) {
            ac = field.getCardRow().get(k);
            if (ac.getSeq() == seq) {
//                return sub拿牌(k);
            }
        }

        return true;
    }

    public boolean actPlayCardBySeq(int seq) {
        int index = 0;
        for (AgesCard ac : currentPlayer.get手牌內政牌區()) {
            if (ac.getSeq() == seq) {
                return act打內政牌(index);
            }
            index++;
        }
        return true;
    }

    public boolean actPlayMilitaryCardBySeq(int seq) {
        int index = 0;
        for (AgesCard ac : currentPlayer.get手牌軍事牌區()) {
            if (ac.getSeq() == seq) {
                System.out.println("actPlayMilitaryCardBySeq seq=" + seq);
                System.out.println("going to actPlayMilitaryCard index=" + index);
                return act打軍事牌(index);
            }
            index++;
        }
        return true;
    }

    /**
     * Caller's responsibility to provide valid index
     *
     * @param index
     * @return
     * @throws AgesException
     */
    private int get卡牌列裡指定編號的卡牌需要支付多少內政點數卡牌列裡指定編號的卡牌_包括已完成的奇蹟(int index) throws AgesException {
        if (index < 0 || index > 12) { // simple protection
            throw new AgesException("get卡牌列裡指定編號的卡牌需要支付多少內政點數卡牌列裡指定編號的卡牌_包括已完成的奇蹟(int index)=> " + index + " is out of range");
        }

        int cost = 從卡牌列指定位置拿牌所需要的內政點數[index];//basic 5,4,4

        if (get卡牌列裡指定編號的卡牌(index).is奇蹟牌()) {
            cost += get當前玩家已完成的奇蹟數();
        }
        return cost;
    }

    private int get當前玩家已完成的奇蹟數() {
        return currentPlayer.get已完成的奇蹟().size();
    }

    private int getCurrentPlayerRemainingCivilPoints() {
        return currentPlayer.get內政點數().getVal();
    }

    private AgesCard get卡牌列裡指定編號的卡牌(int index) {
        return field.getCardRow().get(index);//宣告 card，並將卡牌列裡的指定編號的卡牌指定到這個card變量裡

    }

    private boolean isTakingNOCARD(int index) {
//        AgesCard card = field.getCardRow().get(index);//宣告 card，並將卡牌列裡的指定編號的卡牌指定到這個card變量裡
        if (get卡牌列裡指定編號的卡牌(index).getId() == 1000) {
            System.out.println("這裡沒有牌");
            return true;
        }
        return false;
    }

    private boolean isAnExistingCard(int val) {
        // 1A
        if (val < 0 || val > 12) {
//            System.out.println("這裡沒有牌");
            System.out.println("我無法作出這個動作，我這個位置沒有牌, SHOULD BE 0 TO 12 ONLY!");

            return false;

        }

//        int cost = get卡牌列裡指定編號的卡牌需要支付多少內政點數卡牌列裡指定編號的卡牌_包括已完成的奇蹟(val);
        AgesCard card = field.getCardRow().get(val);//宣告 card，並將卡牌列裡的指定編號的卡牌指定到這個card變量裡

//        Player player = getCurrentPlayer();
//        System.out.println("該牌需要支付多少內政點數:" + cost);
//        目前拿牌不扣內政點
//        if (currentPlayer.get內政點數().getVal() < cost) {
//            System.out.println("NOT ENOUGH 內政點數");
//            return false;
//        }
        // 1B
        if (card.isNOCARD()) {
//            System.out.println("這裡沒有牌");
            System.out.println("我無法作出這個動作，我這個位置沒有牌");

            return false;

        }
        return true;
    }

    private void showWhyNoAction(ReasonWhyNoAction reason) {
        System.out.println("   ...no action, " + reason);
    }

    private void showDebug(String str) {
        System.out.println("   @@@debug, " + str);
    }

    private void showDoneDescription(DoneDescription done) {
        System.out.println("   !!!done, " + done);
    }

    /**
     * <br>1.Not to take NOCARD, including out of range and taken
     * <br>2.Not allow to take card without enough civil points
     * <br> including extra points for built Wonder
     * <br>3.Not allow to take repeat tech card
     * <br>4.Not allow to take wonder card if having any UNDER CONSTRUCTION
     *
     * @param index
     * @return
     */
    private boolean act拿牌(int index) throws AgesException {
        // 0. NOT YET START
        if (field.getCardRow().isEmpty()) {
            showWhyNoAction(ReasonWhyNoAction.GAME_IS_NOT_STARTED_YET);
            return false;
        }

        // 1. NOT ALLOW TO TAKE INVALID CARD
        if (!isAnExistingCard(index)) {
//            System.out.println("You are not taking a valid card from CardRow!");
            showWhyNoAction(ReasonWhyNoAction.NOT_VALID_INDEX_FROM_CARDROW);

            return false;
        }

        // 2 NOT ALLOW TO TAKE CARD WITHOUT ENOUGH CIVIL POINT
        int cost = get卡牌列裡指定編號的卡牌需要支付多少內政點數卡牌列裡指定編號的卡牌_包括已完成的奇蹟(index);
        if (getCurrentPlayerRemainingCivilPoints() < cost) {
//            System.out.println("Current Player doesn't have ENOUGH 內政點數 to take this card");
            showWhyNoAction(ReasonWhyNoAction.內政點數不够);

            return false;
        }

        // 3 Not to take repeat科技牌
        AgesCard card = getCardFromCardRow(index);
        if (card.is科技牌()) {
            if (this.currentPlayer.is已有該張科技牌(card)) {
                showWhyNoAction(ReasonWhyNoAction.拿了重複的科技牌);
                return false;
            }
        }

        //奇蹟 is the only one not go to ON-HAND
        // 4. Not ALLOW to take 奇蹟 when there is any UNDER CONSTRUCTION
        if (card.is奇蹟牌()) {
            if (currentPlayer.get建造中的奇蹟區().size() > 0) {
                showWhyNoAction(ReasonWhyNoAction.UNDER_CONSTRUCTION_IS_NOT_EMPTY);
                return false;
            }
        }

        // to perform action and make transaction
        sub拿牌(index, card, cost);
        showDoneDescription(DoneDescription.TAKE_CARD_SUCCESSFULLY);
        return true;
    }

    private void sub拿牌(int index, AgesCard card, int cost) throws AgesException {
        if (card.is奇蹟牌()) {
            currentPlayer.subMove卡牌列CardToSector(index, card, currentPlayer.建造中的奇蹟區);
//            currentPlayer.init建造中的奇蹟區();
        } else {
            currentPlayer.subMove卡牌列CardToSector(index, card, currentPlayer.手牌內政牌區);
        }
        currentPlayer.sub支付內政點數(cost);
    }

    private void sub在卡牌列拿掉指定位子的卡牌and補上一張空卡牌(int index) {
        field.getCardRow().remove(index);//從卡牌列拿掉剛剛那張card
        field.getCardRow().add(index, field.getNOCARD());//在卡牌列同樣的位子，補上一張空卡
//        System.out.println("卡牌列, index=" + index + " is 空卡牌 now!");
    }

//    private void sub支付內政點數(int cost) {
//        currentPlayer.sub支付內政點數(cost);
////        System.out.println("支付內政點數" + cost);
//    }
    private boolean OLD_actTakeCard(int val) {

//        int[] COST_OF_TAKING_CARD_FROM_CARDROW = {1, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3};
        int cost = 從卡牌列指定位置拿牌所需要的內政點數[val];
//        Player player = getCurrentPlayer();
        System.out.println("該牌需要支付多少內政點數:" + cost);

//        目前拿牌不扣內政點
        if (currentPlayer.get內政點數().getVal() < cost) {
            System.out.println("NOT ENOUGH 內政點數");
            return false;
        }
        AgesCard card = field.getCardRow().get(val);//宣告 card，並將卡牌列裡的指定編號的卡牌指定到這個card變量裡
        if (card.getId() == 1000) {
            System.out.println("這裡沒有牌");
            return true;
        }

        //    如果這張牌是科技牌
        if (card.is科技牌()) {
            System.out.println("這是一張科技牌要寫程式不准拿重複");
            if (this.currentPlayer.is已有該張科技牌(card)) {
                System.out.println("ages:引擎偵測到玩家拿了一張重複的科技牌" + card.getName());
                return false;
//                System.exit(0);
            }
        }

        //奇蹟 is the only one not go to ON-HAND
        if (card.getTag().equals("奇蹟")) {

            System.out.println("DOING...要加計已完成的奇蹟數量");
            List<AgesCard> list = currentPlayer.get建造中的奇蹟區();
            while (list.size() > 0) { // easy for dev
                list.remove(0);
            }
            list.add(card);//當前玩家的手牌加入上一行的card
            field.getCardRow().remove(val);//從卡牌列拿掉剛剛那張card

            field.getCardRow().add(val, field.getNOCARD());//在卡牌列同樣的位子，補上一張空卡

            //
            // to start to track stages
            //
//            currentPlayer.init建造中的奇蹟區();
            currentPlayer.sub支付內政點數(cost);
        } else {//除了奇蹟牌以外
            currentPlayer.get手牌內政牌區().add(card);//當前玩家的手牌加入上一行的card
            field.getCardRow().remove(val);//從卡牌列拿掉剛剛那張card
            field.getCardRow().add(val, field.getNOCARD());//在卡牌列同樣的位子，補上一張空卡
            currentPlayer.sub支付內政點數(cost);
        }
        return true;
    }

    private String doStart() {
        //
        reset();
        set現在階段(內政階段);
//        System.out.println(" just update set現在階段 to be " + get當前時代());
        //卡牌列
        for (int k = 0; k < 13; k++) {
            field.moveOneCard(field.get時代A內政牌(), 0, field.getCardRow());
        }

        for (int k = 0; k < 4; k++) {
            field.moveOneCard(field.get時代A軍事牌(), 0, field.get當前事件());
        }
        // AAA 內政點數=1, BBB 內政點數=2
        for (int turnOrder = 1; turnOrder <= getAllPlayers().size(); turnOrder++) {
//            Player player = geturrentPlayer();
            currentPlayer.get內政點數().setVal(turnOrder);
//            player.subUpdate手牌上限();
            交換玩家();
        }
        //
        //
        //
        p1.sub更新文明板塊上所提供的數據();
        p2.sub更新文明板塊上所提供的數據();
        return "jsut did doStart()";
    }

    private String doChangeTurn() {

        //檢測暴動();
//        currentPlayer.compute當回合文化and科技and軍力();
        //執行生產
        currentPlayer.produce();

        //抽取軍事牌
        int draw = currentPlayer.get軍事點數().getVal();
        if (draw > 3) {
            draw = 3;
        }
        for (int k = 0; k < draw; k++) {
            switch (get當前時代()) {
                case 0:
                    System.out.println("抽取時代 " + get當前時代() + " 的軍事牌");
                    moveOneCard(field.get時代A軍事牌(), 0, currentPlayer.get手牌軍事牌區());
                    break;
                case 1:
                    System.out.println("抽取時代 " + get當前時代() + " 的軍事牌");
                    field.moveOneCard(field.get時代I軍事牌(), 0, currentPlayer.get手牌軍事牌區());
                    break;
                case 2:
                    System.out.println("抽取時代 " + get當前時代() + " 的軍事牌");
                    field.moveOneCard(field.get時代II軍事牌(), 0, currentPlayer.get手牌軍事牌區());
                    break;
                case 3:
                    System.out.println("抽取時代 " + get當前時代() + " 的軍事牌");
                    field.moveOneCard(field.get時代III軍事牌(), 0, currentPlayer.get手牌軍事牌區());
                    break;
                default:
                    System.out.println("不該存在");
            }
        }
        //檢測哪一疊內政牌庫不為0
//        for (int k = 0; k < draw; k++) {
//            if (field.get時代A內政牌().size() != 0) {
//                field.moveOneCard(field.get時代A軍事牌(), 0, field. currentPlayer.get手牌軍事牌區());
//            }else if (field.get時代I內政牌().size() != 0) {
//                field.moveOneCard(field.get時代I軍事牌(), 0, field. currentPlayer.get手牌軍事牌區());
//            }else if (field.get時代II內政牌().size() != 0) {
//                field.moveOneCard(field.get時代II軍事牌(), 0, field.getCurrentPlayer().get手牌軍事牌區());
//            }else if (field.get時代III內政牌().size() != 0) {
//                field.moveOneCard(field.get時代III軍事牌(), 0, field.getCurrentPlayer().get手牌軍事牌區());
//            }
//        }
        currentPlayer.do維持();
        currentPlayer.do腐敗();

        交換玩家();
        // before turn
        if (get當前回合() == 1) {
            return "this round#1 case, not to update手牌上限 refill內政點數軍事點數";
        }

        currentPlayer.subUpdate手牌上限();
        currentPlayer.refill內政點數軍事點數();
        補牌();
        if (get當前回合() > 2) {
            set現在階段(政治階段);
        } else {
            set現在階段(內政階段);

        }
        return " just did doChangeTurn";
    }

    private boolean doChangeTurnV2() {
        for (int k = 0; k < 46; k++) {
            doChangeTurn();
        }
        return true;
    }

    private boolean doStatus(int val) {
//        field.showSector(val);
        System.out.println("DEBUG...");
        return true;
    }

    private void 補牌() {
//        System.out.println("在這裡作補牌");

//        移除前三張，實際作法是移除前三張增加三張三張空卡
        field.getCardRow().remove(0);
        field.getCardRow().remove(0);
        field.getCardRow().remove(0);
        AgesCard temp = new AgesCard();
        temp.setId(1000);
        temp.setName("");
        temp.setAge(4);
        temp.setTag("");
//        field.getCardRow().add(0, temp);//在卡牌列同樣的位子，補上一張空卡
//        field.getCardRow().add(0, temp);//在卡牌列同樣的位子，補上一張空卡

//      左推
        for (int k = 0; k < field.getCardRow().size(); k++) {
            if (field.getCardRow().get(k).getId() == 1000) {
                field.getCardRow().remove(k);
                k--;

            }
        }
        for (int k = field.getCardRow().size(); k < 13; k++) {

            if (field.get時代A內政牌().size() != 0) {
                field.moveOneCard(field.get時代A內政牌(), 0, field.getCardRow());
                for (int x = 0; x < field.get時代A內政牌().size(); x++) {
                    field.get時代A內政牌().remove(0);
                }
            } else if (field.get時代I內政牌().size() != 0) {
                set當前時代(1);
                field.moveOneCard(field.get時代I內政牌(), 0, field.getCardRow());
            } else if (field.get時代II內政牌().size() != 0) {
                set當前時代(2);
                moveOneCard(field.get時代II內政牌(), 0, field.getCardRow());
            } else if (field.get時代III內政牌().size() != 0) {
                //如果還有牌
                set當前時代(3);
                field.moveOneCard(field.get時代III內政牌(), 0, field.getCardRow());
            } else if (field.get時代III內政牌().size() == 0) {
                set當前時代(4);
                System.out.println("沒牌了");
                field.getCardRow().add(k, temp);//在卡牌列同樣的位子，補上一張空卡
                if (currentPlayer == getP1()) {
                    System.out.println("遊戲要結束了");
                } else {
                    System.out.println("遊戲下回合要結束了");
                }
            }
        }

    }

//  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    private String doList() {
//        Set<AgesCard> set=new HashSet(field.getQryCards());

        doList(0);
        return " just did doList(0)";
    }

    private String getSameSizeName(String name) {
        int times = 8 - name.length();
        String cnSpace = "\u3000";
        StringBuilder sb = new StringBuilder();
        for (int k = 0; k < times; k++) {
            sb.append("\u3000");
        }
        return name + sb.toString();
    }

    private boolean doList(int style) {

        Map<Integer, AgesCard> map = new HashMap<>();

        for (AgesCard card : field.getQryCards()) {
            map.put(card.getId(), card);
//           System.out.println(" "+card.getId()+" "+card.getName());
        }

//        System.out.println(" map size is " + map.size());
        Set<Integer> idSet = map.keySet();
        List<Integer> idList = new ArrayList<>(idSet);
        Collections.sort(idList);
        for (Integer key : idList) {
            AgesCard card = map.get(key);
            switch (style) {
                case 0:
                    String yn = "科技牌?是 ";
                    if (card.is科技牌()) {
//                        System.out.println("是 ");
                    } else {
                        yn = "科技牌?否 ";
//                        System.out.println("否 ");
                    }

                    System.out.println(yn + card.getTag() + " " + key + " " + getSameSizeName(card.getName()) + " " + card.getAction().trim() + " " + card.getIconPoints() + " " + card.getEffect());
//                    System.out.println("123456");
                    break;
                case 1:
                    if (card.getAction().length() > 0) {
                        System.out.println(" " + key + " " + getSameSizeName(card.getName()) + " " + card.getAction());

//                        \u3000
                    }
                    break;
                case 2:
                    if (card.getIconPoints().length() > 0) {

                        System.out.println(" " + key + " " + card.getName() + " " + card.getIconPoints());
                    }
                    break;
                case 3:
                    if (card.getEffect().length() > 0) {
                        System.out.println(" " + key + " " + card.getName() + " " + card.getEffect());
                    }
                    break;
                case 4:
                    if (card.getEffectWhite() > 0) {
                        System.out.println(card.getTag() + " " + key + " " + card.getName() + " +白點: " + card.getEffectWhite());
                    }
                    break;
                case 5:
                    if (card.getEffectRed() > 0) {
                        System.out.println(" " + key + " " + card.getName() + " +紅點: " + card.getEffectRed());
                    }
                    break;
                case 6:
                    if (card.getEffectSmile() > 0) {
                        System.out.println(" " + key + " " + card.getName() + " +笑臉: " + card.getEffectSmile());
                    }
                    break;
                case 7:
                    if (card.getTag().equals("行動")) {
                        System.out.println("時代:" + card.getAge() + " " + card.getTag() + " " + key + " " + getSameSizeName(card.getName()) + " " + card.getAction().trim() + " " + card.getIconPoints() + " " + card.getEffect());
                    }
                    break;
                case 8:
                    if (card.getTag().equals("事件")) {
                        System.out.println("時代:" + card.getAge() + " " + card.getTag() + " " + key + " " + getSameSizeName(card.getName()) + " " + card.getAction().trim() + " " + card.getIconPoints() + " " + card.getEffect());
                    }
                    break;
                case 9:
                    if (card.getTag().equals("礦山")) {
                        System.out.println("時代:" + card.getAge() + " " + card.getTag() + " " + key + " " + getSameSizeName(card.getName()) + " " + "張數" + card.getCnt() + " " + card.getAction().trim() + " " + card.getIconPoints() + " " + card.getEffect());
                    }
                    break;
                case 10:
                    if (card.getTag().equals("實驗室")) {
                        System.out.println("時代:" + card.getAge() + " " + card.getTag() + " " + key + " " + getSameSizeName(card.getName()) + " " + card.getAction().trim() + " " + card.getEffect());
                    }
                    break;
                case 11:
                    if (card.getTag().equals("奇蹟")) {
                        System.out.println("時代:" + card.getAge() + " " + card.getTag() + " " + key + " " + getSameSizeName(card.getName()) + " " + card.getAction().trim() + " " + card.getEffect());
                    }
                    break;
                default:
                    System.out.println(" " + key + " " + getSameSizeName(card.getName()) + " " + card.getAction().trim());

            }
        }
        System.out.println("");
        System.out.println(" list 4      SHOW +白點");
        System.out.println(" list 5      SHOW +紅點");
        System.out.println(" list 6      SHOW 笑臉");
        System.out.println(" list 7      SHOW TAG=行動");
        System.out.println(" list 8      SHOW TAG=事件");
        System.out.println(" list 9      SHOW TAG=礦山");
        System.out.println(" list 10      SHOW TAG=實驗室，只顯示Effect");
        System.out.println(" list 11      SHOW TAG=奇蹟");
        return true;

    }

    private boolean act建造(int val) {
//        System.out.println("按卡號build,適合所有的情況, including 奇蹟區");
//        我無法作出這個動作
        if (this.get當前回合() == 0) {
//            System.out.println("no action, GAME IS NOT YET STARTED!");
            showWhyNoAction(ReasonWhyNoAction.GAME_IS_NOT_STARTED_YET);

            return false;
        }

        if (this.get當前回合() == 1) {
//            System.out.println("no action, 按遊戲規則:第一回合，只能拿牌，不能做建造");
            showWhyNoAction(ReasonWhyNoAction.按遊戲規則_第一回合_只能拿牌_不能做建造);
            return false;
        }

        return currentPlayer.act建造(val);
//        currentPlayer.act打軍事牌(val);

    }

    private boolean act升級(int val1, int val2) {
//        System.out.println("按卡號build,適合所有的情況, including 奇蹟區");
//        我無法作出這個動作
        if (this.get當前回合() == 0) {
//            System.out.println("no action, GAME IS NOT YET STARTED!");
            showWhyNoAction(ReasonWhyNoAction.GAME_IS_NOT_STARTED_YET);

            return false;
        }

        if (this.get當前回合() == 1) {
            System.out.println("no action, 按遊戲規則:第一回合，只能拿牌，不能做建造");
//            showWhyNoAction(ReasonWhyNoAction.按遊戲規則_第一回合_只能拿牌_不能做升級);
            return false;
        }

        return currentPlayer.act升級(val1, val2);

    }

    private boolean actDestroy(int val) {
//        System.out.println("按卡號build,適合所有的情況, including 奇蹟區");
        currentPlayer.actDestroy(val);

        return true;
    }

    public Map<String, Integer> getPlayerScore(int player) {
        if (player == 1) {

            return p1.getScore().getMap();

        }

        if (player == 2) {

            return p2.getScore().getMap();

        }

        return null;
    }

    private boolean actAct(int val) {
        System.out.println("執行行動牌");
//           System.out.println(field. currentPlayer.get行動牌暫存區().get(0).getName()+"  效果:"+getCurrentPlayer().get行動牌暫存區().get(0).getAction());
        currentPlayer.actAct(val);

        return true;
    }

    private String do拿牌打牌測試用() {
//        for(int k=0;)
//        sub拿牌(0);
//        act打牌(0);
//
//        sub拿牌(1);
//        act打牌(0);
//
//        sub拿牌(2);
//        act打牌(0);
//
//        sub拿牌(3);
//        act打牌(0);
//
//        sub拿牌(4);
//        act打牌(0);
//        doChangeTurn();
//        field.showSector(1);
        System.out.println("this function has been disabled!");
        return "測試";
    }

    private String doPopulation() {
        if (currentPlayer.do擴充人口()) {
            return "done, just did 擴充人口";
        }
        return "no action";
    }

    private boolean actUpgrade(int p1, int p2) {
        currentPlayer.actUpgrade(p1, p2);
        return true;
    }

    private String doServer() {
        String idx13 = field.getServerStatus();
        server.updateGameLiveCardRow(idx13);

        return " updated DB";
    }

    private String act執行行動牌() {
        System.out.println("執行行動牌");
//           System.out.println(field.getCurrentPlayer().get行動牌暫存區().get(0).getName()+"  效果:"+field.getCurrentPlayer().get行動牌暫存區().get(0).getAction());
        currentPlayer.actActV1();

        return "just did 執行行動牌";
    }
// private void doEvent() {
// 
// }
// 

    private void doChangeStage() {
        if (get現在階段() == 政治階段) {

            InputStreamReader cin = new InputStreamReader(System.in);
            BufferedReader in = new BufferedReader(cin);
            if (currentPlayer.手牌軍事牌區.size() > currentPlayer.get軍事手牌上限().getVal()) {
                System.out.println("你必須將軍事手牌(" + currentPlayer.手牌軍事牌區.size() + ")棄到，軍事手牌上限(" + currentPlayer.get軍事手牌上限().getVal() + ")張");
            } else {
                this.sub從政治階段轉換到內政階段();
            }
        } else {
            this.showDebug("你現在不是政治階段，所以不允許轉換階段");
        }
    }

    private boolean act打軍事牌(int val) {
//            field.getCurrentPlayer().actPlayMilitaryCard(val);

        if (val > currentPlayer.手牌軍事牌區.size() - 1) {
            System.out.println("我無法作出這個動作，我這個位置沒有牌");
            return false;
        }
        AgesCard card = currentPlayer.手牌軍事牌區.get(val);
        if (現在階段 == 政治階段) {
            if (!card.is政治階段可執行的軍事牌()) {
                showWhyNoAction(ReasonWhyNoAction.你無法在政治階段執行這張卡);
//                this.showDebug(card.getName() + "    不預期的錯誤     " + card.getTag());
                return false;
            }
            if (!currentPlayer.is足夠的軍事點數(card)) {
                showWhyNoAction(ReasonWhyNoAction.軍事點數不够);
                return false;
            }
//            currentPlayer.sub從軍事手牌區移除該牌(val);
            currentPlayer.手牌軍事牌區.remove(val);
            currentPlayer.sub玩家政治階段打軍事牌(card);
        }
        if (現在階段 == 內政階段) {
            if (!card.is內政階段可執行的軍事牌()) {
                showWhyNoAction(ReasonWhyNoAction.你無法在內政階段執行這張卡);
                return false;
            }
            if (!currentPlayer.is有1點軍事點數()) {
                showWhyNoAction(ReasonWhyNoAction.軍事點數不够);
                return false;
            }
            currentPlayer.sub玩家內政階段打軍事牌(val);
            return true;
        }
        return true;
    }

    private String shuffle() {
        Collections.shuffle(field.get當前事件());
        return "123";
    }

    private boolean doEvent(int val) throws IOException {
        System.out.println("現在執行卡號ID為:" + val + "的事件");
        AgesCard card = cardFactory.getCardById(val);
        /*限制事件，做檢測先關掉        
         if (!card.getTag().equals("事件")) {
         System.out.println("只執行Tag=事件");
         System.out.println("BUT THIS CARD'S TAG IS " + card.getTag());
         return false;
         }
         */
        //        System.out.println("只執行Tag=事件");
//        System.out.println(field.get現在發生事件().get(0).getAction());
        switch (val) {
            case 12345:
                System.out.println("列出玩家科技牌區域的所有牌數量:");
                System.out.println(this.currentPlayer.get科技牌的檢查區域().size());
//                this.currentPlayer.get科技牌的檢查區域();
                break;
            case 7777:
                System.out.println("測試支付9資源");
                this.currentPlayer.sub支付資源(9);
                break;
            case 8888:
                System.out.println(currentPlayer.get文明所需的笑臉());
                break;
            case 9999:
                getP1().sub更新文明板塊上所提供的數據();
                break;

            case 1005:
                getP1().sub擴充人口();
                getP2().sub擴充人口();
                break;
            case 1006:
                set當前操作玩家(currentPlayer);
                show時代回合玩家階段();

                InputStreamReader cin = new InputStreamReader(System.in);
                BufferedReader in = new BufferedReader(cin);
                System.out.println(get當前操作玩家().getName() + "是否將一名閒置工人免費升級為戰士(Y/N)");
                if (in.readLine().equalsIgnoreCase("Y")) {
                    System.out.println(get當前操作玩家().getName() + "決定閒置工人免費升級為戰士");
                    get當前操作玩家().get步兵區().get(0).setTokenYellow(get當前操作玩家().get步兵區().get(0).getTokenYellow() + 1);
                } else {
                    System.out.println(get當前操作玩家().getName() + "決定不把閒置工人免費升級為戰士");
                }
                交換當前操作玩家();
                show時代回合玩家階段();

                System.out.println(get當前操作玩家().getName() + "是否將一名閒置工人免費升級為戰士(Y/N)");
                if (in.readLine().equalsIgnoreCase("Y")) {
                    System.out.println(get當前操作玩家().getName() + "決定閒置工人免費升級為戰士");
                    get當前操作玩家().get步兵區().get(0).setTokenYellow(get當前操作玩家().get步兵區().get(0).getTokenYellow() + 1);
                } else {
                    System.out.println(get當前操作玩家().getName() + "決定不把閒置工人免費升級為戰士");
                }
                交換當前操作玩家();
                break;

            case 1008:
//                getP1().get內政區
                getP2().sub擴充人口();

                break;
            case 1019:
                getP1().sub獲得資源(9);
                getP2().sub獲得資源(5);
                break;
            case 1129:
//                                行動:所有的文明每有一個不高興的工人，就失去四點文明分數
//                                
//13.2.1 [不高興的工人]=[文明所需要的笑臉]-[文明當前笑臉]
//                System.out.println("AAA所需要的笑臉數=" + field.getP1().get文明所需的笑臉());
//                System.out.println("BBB所需要的笑臉數=" + field.getP2().get文明所需的笑臉());
                int p1不高興的工人數 = getP1().get文明所需的笑臉() - getP1().get笑臉_幸福指數().getVal();
                if (p1不高興的工人數 > 0) {
                    getP1().get文化().addPoints(p1不高興的工人數 * (-4));
                    System.out.println("AAA失去" + p1不高興的工人數 * (4) + "文化");
                }

                int p2不高興的工人數 = getP2().get文明所需的笑臉() - getP2().get笑臉_幸福指數().getVal();

                if (p2不高興的工人數 > 0) {
                    getP2().get文化().addPoints(p2不高興的工人數 * (-4));
                    System.out.println("BBB失去" + p2不高興的工人數 * (4) + "文化");

                }

            case 1051:
                showDebug("ACTION:每擁有一個不幸福的工人,下回合將減少兩個內政行動");
                showDebug("目標範圍:全部的玩家");

                showDebug("p1不幸福的工人=" + p1.get不幸福的工人());
                showDebug("p2不幸福的工人=" + p2.get不幸福的工人());
                if (p1.get不幸福的工人() > 0) {
                    showDebug("P1下回合將減少兩個內政行動");
                    p1.set下回合失去的內政點數(2 * p1.get不幸福的工人());
                }
                if (p2.get不幸福的工人() > 0) {
                    showDebug("P2下回合將減少兩個內政行動");
                    p2.set下回合失去的內政點數(2 * p1.get不幸福的工人());
                }

                break;
            /*
             case :
             break;
             case :
             break;
             case :
             break;
             case :
             break;
             case :
             break;
             case :
             break;
             case :
             break;
             */

            default:
                System.out.println("DOING... NEED TO PROGRAM FOR THIS EVENT " + val);
        }
        return true;
    }

    private boolean act丟棄軍事牌(int index) {
        // 1
        if (index > currentPlayer.get手牌軍事牌區().size() - 1 || index < 0) {
            System.out.println("我無法作出這個動作，我這個位置沒有牌");
            return false;
        }
//        軍事手牌上限2  = 軍事手牌區1張
        if (currentPlayer.get軍事手牌上限().getVal() >= currentPlayer.get手牌軍事牌區().size()) {
            System.out.println("你不能再棄軍事牌了，按照max的理解，規則是要保留手上的軍事牌數，如同軍事");
            return false;
        }
        currentPlayer.act玩家丟棄軍事牌(index);
        return true;
    }

//    private void show(int i) {
//        if (i == 10) {
//
////            field.showSector(i);
//            return;
//        }
//        if (i == 123) {
////            field.showSector(i);
//            return;
//        }
////        field.showSector(i);
//        p1.show();
//        p2.show();
//    }
    public class Field {

//    public String getCurrentStage(){
//        if (當前時代==政治階段){
//            return "政治階段";
//        }
//        if (當前時代==內政階段){
//            return "內政階段";
//        }
//        System.out.println("what is 當前時代?"+當前時代);
//        
//        
//        return "???";
//    }
        public List<AgesCard> get現在發生事件() {
            return 現在發生事件;
        }

        public void set現在發生事件(List<AgesCard> val) {
            現在發生事件 = val;
        }

        public AgesCard getNOCARD() {
            return cardFactory.getNOCARD();
        }

        public List<AgesCard> getAllCards() {
            return allCards;
        }

        public void moveOneCard(List<AgesCard> from, int index, List<AgesCard> to) {

            to.add(from.remove(index));
        }

        public List<AgesCard> getCardRow() {
            return 卡牌列;
        }

        public List<AgesCard> get時代A內政牌() {
            return 時代A內政牌;
        }

        public List<AgesCard> get時代I內政牌() {
            return 時代I內政牌;
        }

        public List<AgesCard> get時代II內政牌() {
            return 時代II內政牌;
        }

        public List<AgesCard> get時代III內政牌() {
            return 時代III內政牌;
        }

        public List<AgesCard> get時代A軍事牌() {
            return 時代A軍事牌;
        }

        public List<AgesCard> get軍事牌回收區() {
            return 軍事牌回收區;
        }

        public AgesCard getCardByName(List<AgesCard> list, String name) {
            //   List<Card> list=new List<>();
            for (int k = 0; k < list.size(); k++) {
                if (list.get(k).getName().equals(name)) {
                    return list.get(k);
                }
            }
            return null;
        }

        public List<AgesCard> get時代I軍事牌() {
            return 時代I軍事牌;
        }

        public List<AgesCard> get時代II軍事牌() {
            return 時代II軍事牌;
        }

        public List<AgesCard> get時代III軍事牌() {
            return 時代III軍事牌;
        }

        public List<AgesCard> get未來事件() {
            return 未來事件;
        }

        public List<AgesCard> get當前事件() {
            return 當前事件;
        }

//    public Player getCurrentPlayer() {
//        return currentPlayer;
//    }
        public List<AgesCard> getAgeCivil(List<AgesCard> list, int age) {
            List<AgesCard> newList = new ArrayList<>();
            for (AgesCard card : list) {
                if (card.getCivilMilitary().endsWith("內政")) {
                    if (card.getAge() == age) {
                        newList.add(card);
                        list.remove(card);
                    }
                }
            }
            return newList;
        }

        public List<AgesCard> getAgeMilitary(List<AgesCard> list, int age) {
            List<AgesCard> newList = new ArrayList<>();
            for (AgesCard card : list) {
                if (card.getCivilMilitary().endsWith("軍事")) {
                    if (card.getAge() == age) {
                        newList.add(card);
                    }
                }
            }
            return newList;
        }

        public void showCardInfo(int id) {
//        System.out.println(" qry card id=" + id);
//        System.out.println(" size=" + qryCards.size());

            for (AgesCard card : qryCards) {
                if (card.getId() == id) {
                    System.out.println("編號:" + card.getId());
                    System.out.println("卡名:" + card.getName());
                    System.out.println("類型:" + card.getCivilMilitary());
                    System.out.println("時代:" + card.getAge());
                    System.out.println("右上:" + card.getTag());
                    System.out.println("行動:" + card.getAction());
                    System.out.println("成本:" + card.getIconPoints());
                    System.out.println("效果:" + card.getEffect());
                    System.out.println(card.toString());

//                System.out.println(card.getName());
                    return;
                }
            }
        }

        public Field() {
            init();
        }

        public void init() {

        }

        public List<AgesCard> getQryCards() {
            return qryCards;
        }

        public void show(List<AgesCard> list, String title) {

            switch (title) {
                case "卡牌列":
//                System.out.print("\u3000");
//                System.out.print(title + " (" + list.size() + ")");
                    System.out.print("卡牌列\u3000");
                    if (list.size() == 0) {
                        return;
                    }
                    System.out.print("(1)");
                    for (int k = 0; k <= 4; k++) {
                        System.out.print("" + k + list.get(k).toString(2) + "\t");
                    }
                    System.out.print("\n\u3000\u3000\u3000\u3000(2)");
                    for (int k = 5; k <= 8; k++) {
                        System.out.print("" + k + list.get(k).toString(2) + "\t");
                    }
                    System.out.print("\n\u3000\u3000\u3000\u3000(3)");
                    for (int k = 9; k <= 12; k++) {
                        System.out.print("" + k + list.get(k).toString(2) + "\t");
                    }
//                System.out.println("");
                    break;
                default:
                    System.out.println("");
                    System.out.print(title + " (" + list.size() + ")");
                    for (AgesCard card : list) {
                        System.out.print("" + card.toString(2));
                    }

            }

        }
//    
//    public void showSector(int style) {
//        switch (style) {
//            case 0:
//                round.showSector();
//                卡牌列.showSector(1);
//                System.out.println("Current Player: " + currentPlayer.name);
//                break;
//            case 1:
//                p1.showSector(1);
//                break;
//            case 2:
//                p2.showSector(1);
//                break;
//            case 11:
//                p1.showSector(2);
//                break;
//            case 22:
//                p2.showSector(2);
//                break;
//
//            default:
//                showSector();
//        }
//    }

//    public void showSector() {
//        round.showSector();
//        System.out.println("\nCurrent Player: " + currentPlayer.name);
//        showSector(卡牌列, "卡牌列");
//        showSector(時代A內政牌, "時代A內政牌");
//        showSector(時代I內政牌, "時代I內政牌");
//        showSector(時代II內政牌, "時代II內政牌");
//        showSector(時代III內政牌, "時代III內政牌");
//        showSector(時代A軍事牌, "時代A軍事牌");
//        showSector(時代I軍事牌, "時代I軍事牌");
//        showSector(時代II軍事牌, "時代II軍事牌");
//        showSector(時代III軍事牌, "時代III軍事牌");
//        for (Player player : allPlayers) {
//            player.showSector();
//        }
//    }
        public void show卡牌列() {
            show(卡牌列, "卡牌列");
            show(未來事件, "未來事件");
//            show(現在事件,"現在事件");
            System.out.println("");

        }

        public void show軍事牌回收區() {
            show(軍事牌回收區, "軍事牌回收區");
            System.out.println("");

        }

        public void show當前事件未來事件() {
            show(現在發生事件, "現在發生事件");
            show(當前事件, "當前事件");
            show(未來事件, "未來事件");
        }

        public void show時代AIII軍事牌() {
            show(時代A軍事牌, "時代A軍事牌");
            show(時代I軍事牌, "時代I軍事牌");
            show(時代II軍事牌, "時代II軍事牌");
            show(時代III軍事牌, "時代III軍事牌");
        }

//        public void showSector(int style) {
//
//            switch (style) {
//                case 0:
////                System.out.println("**********************當前時代:" + this.當前時代 + "  回合:" + round.getVal() + "  Current Player: " + currentPlayer.getName() + " ******************************************************");
//                    showSector(卡牌列, "卡牌列");
//                    showSector(當前事件, "當前事件");
//                    showSector(未來事件, "未來事件");
//                    showSector(現在發生事件, "現在發生事件");
//                    showSector(時代A軍事牌, "時代A軍事牌");
//                    showSector(時代I軍事牌, "時代I軍事牌");
//                    showSector(時代II軍事牌, "時代II軍事牌");
//                    showSector(時代III軍事牌, "時代III軍事牌");
////                allPlayers.stream().forEach((p) -> {
////                    p.showSector();
////                });
//                    break;
//                case 1:
////                System.out.println("**********************當前時代:" + this.當前時代 + "  回合:" + round.getVal() + "  Current Player: " + currentPlayer.getName() + " ******************************************************");
//                    showSector(卡牌列, "卡牌列");
//                    showSector(時代A內政牌, "時代A內政牌");
//                    showSector(時代I內政牌, "時代I內政牌");
//                    showSector(時代II內政牌, "時代II內政牌");
//                    showSector(時代III內政牌, "時代III內政牌");
//                    showSector(時代A軍事牌, "時代A軍事牌");
//                    showSector(時代I軍事牌, "時代I軍事牌");
//                    showSector(時代II軍事牌, "時代II軍事牌");
//                    showSector(時代III軍事牌, "時代III軍事牌");
////                allPlayers.stream().forEach((p) -> {
////                    p.showSector();
////                });
//                    break;
//                case 10:
//                    show時代回合玩家階段();
//                    break;
//
//                case 123:
//                    show時代回合玩家階段();
//                    showSector(卡牌列, "卡牌列");
//                    showSector(當前事件, "當前事件");
//                    showSector(未來事件, "未來事件");
//                    showSector(現在發生事件, "現在發生事件");
//                    showSector(時代A軍事牌, "時代A軍事牌");
//                    showSector(時代I軍事牌, "時代I軍事牌");
//                    showSector(時代II軍事牌, "時代II軍事牌");
//                    showSector(時代III軍事牌, "時代III軍事牌");
//                    System.out.println("");
//                    break;
//                default:
//                    showSector(卡牌列, "卡牌列");
////                currentPlayer.showSector();
//            }
//
//        }
        public String getServerStatus() {
            StringBuilder sb = new StringBuilder();
            for (AgesCard card : 卡牌列) {
                sb.append(card.getId()).append(",");
            }
            return sb.toString();
        }

    }

    public class Player {

        Score score;

        Token token黃;

        Token token藍;

//        final int 人力庫_黃 = 1;
//        final int 工人區_黃 = 2;
//        final int 資源庫_藍 = 3;
        public Token getToken黃() {

            return token黃;

        }

        public Token getToken藍() {

            return token藍;

        }

        Player() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
//    private List<AgesCard> 行政牌暫存區;

        public void moveOneCard(List<AgesCard> from, int index, List<AgesCard> to) {

            to.add(from.remove(index));
        }
//        public void moveOneCard(AgesCard> from,Ages card , List<AgesCard> to) {
//
//            to.add(from.remove(index));
//        }

        public String getName() {
            return name;
        }

        public boolean sub支付內政點數(int cost) {
            if (目前內政點數.getVal() >= cost) {
                int old內政點數 = 目前內政點數.getVal();
                目前內政點數.addPoints((-1) * cost);
                showDebug("內政點數 " + old內政點數 + " => " + 目前內政點數.getVal());
                return true;
            } else {
                showDebug("行動失敗，不夠支付內政點數");
                return false;
            }
        }

        public boolean sub支付軍事點數(int cost) {
            if (目前軍事點數.getVal() >= cost) {

                int old軍事點數 = 目前軍事點數.getVal();
                目前軍事點數.addPoints((-1) * cost);
                showDebug("軍事點數 " + old軍事點數 + " => " + 目前軍事點數.getVal());
                return true;
            } else {
                showDebug("行動失敗，不夠支付軍事點數");
                return false;
            }
        }

        public boolean sub支付科技點數(int cost) {
            if (this.科技.getVal() >= cost) {

                int old科技點數 = 科技.getVal();
                科技.addPoints((-1) * cost);
                showDebug("科技點數 " + old科技點數 + " => " + 科技.getVal());
                return true;
            } else {
                showDebug("行動失敗，不夠支付科技點數");
                return false;
            }
        }

        public Points get內政點數() {
            return 目前內政點數;
        }

        public Points get軍事點數() {
            return 目前軍事點數;
        }

        public void do維持() {
            int val = get人力庫_黃點().getVal();
            int amt = 0;

            if (val <= 16) {
                amt = AgesCommon.array回合維持支付食物對照表[val];
                showDebug("回合維持支付食物=" + amt);
            }
            showDebug("!!!!!!!!!!!!!!!!!!!!!!!getAvailable食物=" + getAvailable食物());
            showDebug("!!!!!!!!!!!!!!!!!!!!!!!amt=" + amt);
            if (this.getAvailable食物() < amt) {
//                showWhyNoAction(ReasonWhyNoAction.食物不够);

                showDebug("!!!!!!!!!!!!!!!!!!!!!!!食物不夠準備扣分=" + amt);
                showDebug("!!!!!!!!!!!!!!!!!!!!!!!getAvailable食物=" + getAvailable食物());
                showDebug("!!!!!!!!!!!!!!!!!!!!!!!amt=" + amt);
                int val2 = (amt - this.getAvailable食物()) * (-4);
                sub支付食物(this.getAvailable食物());
                this.sub增加文化值(val2);
            } else {
                sub支付食物(amt);
            }
        }

        /**
         * 假設獲得9點資源，該玩家有礦山科技等級0.1.3 系統先從最高等級(III
         * 石油:5石頭)的礦山先放置9點資源，於是9/5=1...4，該礦山藍點數+1
         * 並且剩下4點再從次等級(1等:2石頭)的礦山等級放置4/2=2....0，該礦山藍點點數+2 結束
         *
         * @param val
         */
        public void sub獲得資源(int val) {
            int b = 0;
            System.out.println("正在開發獲得資源");
            System.out.println(getScore().getMap());
            System.out.println(this.getToken藍().getMap());
//       while(val) 
//        effectStone=5,
//        時代:0 礦山 1032 青銅　　　　　　  石頭:2 石頭:1
//時代:1 礦山 1099 鐵礦　　　　　　  燈泡:5;石頭:5 石頭:2
//時代:2 礦山 1150 煤礦　　　　　　  燈泡:7;石頭:8 石頭:3
//時代:3 礦山 1234 石油　　　　　　  燈泡:9;石頭:11 石頭:5

            for (int x = this.礦山區.size() - 1; x >= 0; x--) {
                System.out.println("獲得[" + val + "]點資源");
                System.out.println("這是" + 礦山區.get(x).getAge() + "的礦山");

                //設定礦山藍點為 原本藍點+獲得藍點/礦山效果:石頭
                System.out.println("目標藍點=" + this.礦山區.get(x).getEffectStone() + "點資源");
                System.out.println("現在有" + this.礦山區.get(x).getTokenBlue() + "點藍點，要+" + val / this.礦山區.get(x).getEffectStone() + "個藍點");
                while ((val > this.礦山區.get(x).getEffectStone()) && (this.get資源庫_藍點().getVal() > 0)) {
                    System.out.println("資源庫_藍點移至礦山區...礦山區的藍點值:" + 礦山區.get(x).getEffectStone());
                    this.subMove資源庫藍點to卡牌(礦山區.get(x), 1);
//                    this.get資源庫_藍點().setVal(this.get資源庫_藍點().getVal() - 1);
//                    this.礦山區.get(x).setTokenBlue(this.礦山區.get(x).getTokenBlue() + 1);
                }
//                this.礦山區.get(x).setTokenBlue(this.礦山區.get(x).getTokenBlue() + (val / this.礦山區.get(x).getEffectStone()));
//                b=b+val % this.礦山區.get(x).getEffectStone();
//            System.out.println("現在將時代"+礦山區.get(x).getAge()+"的礦山放入"+val/this.礦山區.get(x).getEffectStone()+"個藍點");
//                val = val % this.礦山區.get(x).getEffectStone();

                System.out.println("剩下[" + val + "]點資源要處理");

            }
        }

//        public void 
        public void sub獲得食物(int val) {
            showDebug("DOING...sub獲得食物" + val);
            for (int x = 農場區.size() - 1; x >= 0; x--) {
                showDebug("獲得[" + val + "]點資源");
                showDebug("這是" + 農場區.get(x).getAge() + "的FOOD");

                //設定礦山藍點為 原本藍點+獲得藍點/礦山效果:石頭
                showDebug("目標藍點=" + this.農場區.get(x).getEffectFood() + " food");
                showDebug("現在有" + this.農場區.get(x).getTokenBlue() + "點藍點，要+" + val / 農場區.get(x).getEffectFood() + "個藍點");
//                礦山區.get(x).setTokenBlue(農場區.get(x).getTokenBlue() + (val / 礦山區.get(x).getEffectFood()));
                while ((val > this.農場區.get(x).getEffectFood()) && (this.get資源庫_藍點().getVal() > 0)) {
                    System.out.println("資源庫_藍點移至農場區...農場區的藍點值:" + 農場區.get(x).getEffectFood());
                    this.subMove資源庫藍點to卡牌(農場區.get(x), 1);
//                    this.get資源庫_藍點().setVal(this.get資源庫_藍點().getVal() - 1);
//                    this.農場區.get(x).setTokenBlue(this.農場區.get(x).getTokenBlue() + 1);
                }

//            System.out.println("現在將時代"+礦山區.get(x).getAge()+"的礦山放入"+val/this.礦山區.get(x).getEffectStone()+"個藍點");
//                val = val % 農場區.get(x).getEffectFood();
                showDebug("剩下[" + val + "]點資源要處理");

            }
        }

        public Points get建築上限() {
            return 建築上限;
        }

        public Points get內政手牌上限() {
            return 內政點數上限;
        }
//public boolean is具有相同的牌()
//{
//    
//}

        public void sub更新文明板塊上所提供的數據() {
//        暫存應用區
            System.out.println("sub更新文明板塊上所提供的數據");;
            int val = 0;
            int 內政點數val = 0;
            int 軍事點數val = 0;
            int 建築上限val = 0;
            int 殖民點數val = 0;
            int 文化增加val = 0;
            int 科技增加val = 0;
            int 軍力val = 0;
            int 笑臉val = 0;

            暫存應用區 = new ArrayList<>();
            暫存應用區.addAll(實驗室);

            暫存應用區.addAll(圖書館區);
            暫存應用區.addAll(劇院區);
            暫存應用區.addAll(競技場區);
            暫存應用區.addAll(神廟區);

            暫存應用區.addAll(步兵區);
            暫存應用區.addAll(騎兵區);
            暫存應用區.addAll(炮兵區);
            暫存應用區.addAll(空軍區);

            暫存應用區.addAll(政府區);
            暫存應用區.addAll(領袖區);
            暫存應用區.addAll(this.已完成的奇蹟);

            暫存應用區.addAll(內政區);
            暫存應用區.addAll(殖民區);
            暫存應用區.addAll(軍事區);
            for (int x = 0; x < 暫存應用區.size(); x++) {
//            System.out.println(暫存應用區.get(x).getName());
                AgesCard card = 暫存應用區.get(x);
                switch (card.getTag()) {
                    case "實驗室":
                    case "圖書館":
                    case "劇院":
                    case "競技場":
                    case "神廟":
                    case "步兵":
                    case "騎兵":
                    case "炮兵":
                    case "空軍":
                        if (暫存應用區.get(x).getEffectIdea() != 0) {
                            科技增加val = 科技增加val + (暫存應用區.get(x).getTokenYellow() * 暫存應用區.get(x).getEffectIdea());
                        }
                        if (暫存應用區.get(x).getEffectMusic() != 0) {
                            文化增加val = 文化增加val + (暫存應用區.get(x).getTokenYellow() * 暫存應用區.get(x).getEffectMusic());
                        }
                        if (暫存應用區.get(x).getEffectSmile() != 0) {
                            笑臉val = 笑臉val + (暫存應用區.get(x).getTokenYellow() * 暫存應用區.get(x).getEffectSmile());
                        }
                        if (暫存應用區.get(x).getEffectWeapon() != 0) {
                            軍力val = 軍力val + (暫存應用區.get(x).getTokenYellow() * 暫存應用區.get(x).getEffectWeapon());
                        }
                        break;
                    case "政府":
                    case "領袖":
                    case "奇蹟":
                        if (暫存應用區.get(x).getEffectIdea() != 0) {
                            科技增加val = 科技增加val + (暫存應用區.get(x).getEffectIdea());
                        }
                        if (暫存應用區.get(x).getEffectMusic() != 0) {
                            文化增加val = 文化增加val + (暫存應用區.get(x).getEffectMusic());
                        }
                        if (暫存應用區.get(x).getEffectSmile() != 0) {
                            笑臉val = 笑臉val + (暫存應用區.get(x).getEffectSmile());
                        }
                        if (暫存應用區.get(x).getEffectWeapon() != 0) {
                            軍力val = 軍力val + (暫存應用區.get(x).getEffectWeapon());
                        }
                        break;
                    case "內政":
                    case "殖民":
                    case "軍事":
                        break;
                    default:

                }
            }
            int old科技生產_當回合 = 科技生產_當回合.getVal();
            int old笑臉_幸福指數 = 笑臉_幸福指數.getVal();
            int old文化生產_當回合 = 文化生產_當回合.getVal();
            int old軍力 = this.軍力.getVal();

            if (old科技生產_當回合 != 科技增加val) {
                科技生產_當回合.setVal(科技增加val);
                showDebug("科技生產_當回合 " + old科技生產_當回合 + " => " + 科技增加val);
            }

            if (old笑臉_幸福指數 != 笑臉val) {
                笑臉_幸福指數.setVal(笑臉val);
                showDebug("笑臉_幸福指數 " + old笑臉_幸福指數 + " => " + 笑臉val);

            }
            if (old文化生產_當回合 != 文化增加val) {
                文化生產_當回合.setVal(文化增加val);
                showDebug("文化生產_當回合 " + old文化生產_當回合 + " => " + 文化增加val);
            }

            if (old軍力 != 軍力val) {
                軍力.setVal(軍力val);
                showDebug("軍力 " + old軍力 + " => " + 軍力val);

            }

        }

        public int getHowManyMoreStage建造奇蹟() {
            if (建造中的奇蹟區.isEmpty()) {
                return -1; // no wonder card under construction
            }

            List stageList = new ArrayList<>();
            AgesCard card = 建造中的奇蹟區.get(0);

//            System.out.println(" WE KNOW CURRENT 建造中的奇蹟區 IS " + card.getName() + " " + card.getIconPoints());
            String cost1 = card.getIconPoints(); //萬里長城 奇蹟石頭:2-2-3-2
            String[] cost2 = cost1.split(":");
            String cost3 = cost2[1]; //2-2-3-2
//            System.out.println(" COST IS " + cost3);
            String[] cost4 = cost3.split("-");

            for (String cost5 : cost4) {//2 2 3 2
//                System.out.println(" " + cost5);
                int cost6 = Integer.parseInt(cost5);
                stageList.add(cost6);
            }
            return card.getTokenBlue() - stageList.size();
        }

        public List<Integer> getRequired資源to建造奇蹟ByStage() {
            List stageList = new ArrayList<>();
            AgesCard card = 建造中的奇蹟區.get(0);

//            System.out.println(" WE KNOW CURRENT 建造中的奇蹟區 IS " + card.getName() + " " + card.getIconPoints());
            String cost1 = card.getIconPoints(); //萬里長城 奇蹟石頭:2-2-3-2
            String[] cost2 = cost1.split(":");
            String cost3 = cost2[1]; //2-2-3-2
//            System.out.println(" COST IS " + cost3);
            String[] cost4 = cost3.split("-");

            for (String cost5 : cost4) {//2 2 3 2
//                System.out.println(" " + cost5);
                int cost6 = Integer.parseInt(cost5);
                stageList.add(cost6);
            }
            return stageList;
        }

        public int getRequired資源to建造奇蹟() {
            AgesCard card = 建造中的奇蹟區.get(0);
            int blueCnt = card.getTokenBlue();
            // when card's blue=0, to build stage 0
            // when card's blue=1, to build stage 1
            // ...
            return getRequired資源to建造奇蹟ByStage().get(blueCnt);
        }

        public Points get軍事手牌上限() {
            return 軍事點數上限;
        }

        public Points get殖民點數() {
            return 殖民點數;
        }

        public Score getScore() {
            return score;
        }

        public void produce() {
            produce文化();
            produce科技();
            produce農場();
            showDoneDescription(DoneDescription.農場_執行生產);
            showDebug("#################3TODO..., PAY FOOD!!!");
            produce礦山();

//            Iterator iterator = token黃.getMap().entrySet().iterator();
//
//            while (iterator.hasNext()) {
//
//                Map.Entry<Integer, Integer> entry = (Map.Entry<Integer, Integer>) iterator.next();
//
//                if (entry.getKey() > 1000) {
//
//                    System.out.println(" K:" + entry.getKey() + " V:" + entry.getValue());
//
////                    token藍.moveTokensFromAtoB(entry.getValue(), 資源庫_藍, entry.getKey());
//
//                }
//
//            }
        }

        private void produce文化() {
            get文化().addPoints(get文化生產_當回合().getVal());
        }

        private void produce農場() {
            for (AgesCard card : 農場區) {
//                card.setTokenBlue(card.getTokenBlue() + card.getTokenYellow());
//                get資源庫_藍點().addPoints(-card.getTokenYellow());
                subMove資源庫藍點to卡牌(card, card.getTokenYellow());
            }
        }

        private void produce礦山() {
            for (AgesCard card : 礦山區) {

//                card.setTokenBlue(card.getTokenBlue() + card.getTokenYellow());
//                get資源庫_藍點().addPoints(-card.getTokenYellow());
                subMove資源庫藍點to卡牌(card, card.getTokenYellow());
            }
        }

        private void produce科技() {
            get科技().addPoints(get科技生產_當回合().getVal());
        }

        public Points get文化() {
            return 文化;
        }

        public Points get文化生產_當回合() {
            return 文化生產_當回合;
        }

        public Points get科技() {
            return 科技;
        }

        public Points get科技生產_當回合() {
            return 科技生產_當回合;
        }

        public Points get軍力() {
            return 軍力;
        }

        public Points get資源庫_藍點() {
            return 資源庫_藍點;
        }

        public Points get人力庫_黃點() {
            return 人力庫_黃點;
        }

        public Points get笑臉_幸福指數() {
            return 笑臉_幸福指數;
        }

        public Points get工人區_黃點() {
            return 工人區_黃點;
        }

        public List<AgesCard> get領袖區() {
            return 領袖區;
        }

        public List<AgesCard> getOnTable() {
            List<AgesCard> onTable;
            onTable = new ArrayList<>();
            onTable.addAll(政府區);
            onTable.addAll(領袖區);
            onTable.addAll(建造中的奇蹟區);

            onTable.addAll(實驗室);
            onTable.addAll(神廟區);
            onTable.addAll(農場區);
            onTable.addAll(礦山區);
            onTable.addAll(步兵區);
            onTable.addAll(已完成的奇蹟);
            onTable.addAll(劇院區);
            onTable.addAll(圖書館區);
            onTable.addAll(殖民領土區);
            onTable.addAll(炮兵區);
            onTable.addAll(特殊科技區);
            onTable.addAll(競技場區);
            onTable.addAll(空軍區);
            onTable.addAll(騎兵區);
            onTable.addAll(未分類區);
            onTable.addAll(戰術區);
            onTable.addAll(戰爭區);

            onTable.addAll(內政區);
            onTable.addAll(軍事區);
            onTable.addAll(建築區);
            onTable.addAll(殖民區);
            onTable.addAll(行動牌區);
            return onTable;

        }

        public List<AgesCard> get戰爭區() {
            return 戰爭區;
        }

        public void set戰爭區(List<AgesCard> 戰爭區) {
            this.戰爭區 = 戰爭區;
        }

        public List<AgesCard> get戰術區() {
            return 戰術區;
        }

        public void set戰術區(List<AgesCard> 戰術區) {
            this.戰術區 = 戰術區;
        }

        public List<AgesCard> get行動區() {
            return 行動區;
        }

        public void set行動區(List<AgesCard> 行動區) {
            this.行動區 = 行動區;
        }

        public List<AgesCard> get政府區() {
            return 政府區;
        }

        public int getAvailable資源() {
            int val = 0;
            for (int x = 0; x < this.礦山區.size(); x++) {
                val = val + 礦山區.get(x).getTokenBlue() * 礦山區.get(x).getEffectStone();
            }
//            System.out.println("玩家目前有 " + val + " 點資源");
            return val;
        }

        public int getAvailable食物() {
            int val = 0;
            for (int x = 0; x < this.農場區.size(); x++) {
                val = val + 農場區.get(x).getTokenBlue() * 農場區.get(x).getEffectFood();
            }
//            System.out.println("玩家目前有 " + val + " 點資源");
            return val;
        }

        public String get資源明細() {
            int val = 0;
//            int val
            String str = "";

            for (int x = 0; x < this.礦山區.size(); x++) {
                val = val + 礦山區.get(x).getTokenBlue() * 礦山區.get(x).getEffectStone();
                str = str + "(" + 礦山區.get(x).getEffectStone() + "*" + 礦山區.get(x).getTokenBlue() + ")";
                if (x != 礦山區.size() - 1) {
                    str += "+";
                }
            }
            str = val + "=" + str;
//            System.out.println("玩家目前有 " + val + " 點資源");
            return str;
        }

        public String get食物明細() {
            int val = 0;
//            int val
            String str = "";

            for (int x = 0; x < this.農場區.size(); x++) {
                val = val + 農場區.get(x).getTokenBlue() * 農場區.get(x).getEffectFood();
                str = str + "(" + 農場區.get(x).getEffectFood() + "*" + 農場區.get(x).getTokenBlue() + ")";
                if (x != 農場區.size() - 1) {
                    str += "+";
                }
            }
            str = val + "=" + str;
//            System.out.println("玩家目前有 " + val + " 點資源");
            return str;
        }

        public int get食物() {
            int val = 0;
            for (int x = 0; x < this.農場區.size(); x++) {
                val = val + 農場區.get(x).getTokenBlue() * 農場區.get(x).getEffectFood();
            }
            System.out.println("玩家目前有 " + val + " 點食物");
            return val;
        }

        public List<AgesCard> get實驗室() {
            return 實驗室;
        }

        public List<AgesCard> get神廟區() {
            return 神廟區;
        }

        public Points get額外用於建造軍事單位的資源() {
            return 額外用於建造軍事單位的資源;
        }

//        public void sub支付食物(int amt) {
//            System.out.println("############### TODO sub支付食物 ##############3");
//        }
        /**
         * 給予方法int成本 參照該玩家面板上最好的方法支付資源 舉例:礦山等級(加權指數)/藍點 A(1)/5 I(2)/4 II(3)/1
         * III(5)/1 支付5點 A/0 I/4 II/1 III/1 支付6點>支付7點 A/0 I/3 II/1 III/1 獲得1點資源
         * A/1 I/3 II/1 III/1 === AAA === 資源:17 =(1*5) + (2*6) 食物:0
         *
         * @param amt
         */
        public void sub支付資源(int amt) {
//            System.out.println("現在開始支付資源");
            int val = amt;
            for (int x = 0; x < this.礦山區.size(); x++) {
                while ((val > 0) && (this.礦山區.get(x).getTokenBlue() != 0)) {
                    val = val - 礦山區.get(x).getEffectStone();
                    subMove卡牌藍點to資源庫(礦山區.get(x), 1);
                }
            }
            if (val < 0) {
                sub獲得資源(-val);
            }
//            System.out.println("done, 支付資源:" + amt);
        }

        public void sub增加文化值(int amt) {
            this.文化.setVal(文化.getVal() + amt);
            if (文化.getVal() < 0) {
                文化.setVal(0);
            }
        }

        public void sub支付食物(int amt) {
//            System.out.println("現在開始支付資源");
            int val = amt;
            for (int x = 0; x < this.農場區.size(); x++) {
                while ((val > 0) && (this.農場區.get(x).getTokenBlue() != 0)) {
                    val = val - 農場區.get(x).getEffectFood();
//                    System.out.println("還需支付的資源" + val);
//                    礦山區.get(x).setTokenBlue(礦山區.get(x).getTokenBlue() - 1);
//                    資源庫_藍點.addPoints(1);
                    subMove卡牌藍點to資源庫(農場區.get(x), 1);
                }
            }
            if (val < 0) {
                sub獲得食物(-val);
            }
//            System.out.println("done, 支付資源:" + amt);
            showDoneDescription(DoneDescription.PAY_FOOD);
        }

        public void set額外用於建造軍事單位的資源(Points 額外用於建造軍事單位的資源) {
            this.額外用於建造軍事單位的資源 = 額外用於建造軍事單位的資源;
        }

        public List<AgesCard> get未分類區() {
            return 未分類區;
        }

        public void set未分類區(List<AgesCard> 未分類區) {
            this.未分類區 = 未分類區;
        }

        public List<AgesCard> get騎兵區() {
            return 騎兵區;
        }

        public void set騎兵區(List<AgesCard> 騎兵區) {
            this.騎兵區 = 騎兵區;
        }

//       onTable.addAll(政府區);
//    onTable.addAll(領袖區);
//    onTable.addAll(建造中的奇蹟區);
//    
//    onTable.addAll(實驗室);
//    onTable.addAll(神廟區);
        public List<AgesCard> getSector(int k) {
            switch (k) {
                case Sector.政府區:
                    return 政府區;
                case Sector.領袖區:
                    return 領袖區;
                case Sector.建造中的奇蹟區:
                    return 建造中的奇蹟區;
                case Sector.實驗室:
                    return 實驗室;
                case Sector.神廟區:
                    return 神廟區;
                case Sector.農場區:
                    return 農場區;
                case Sector.礦山區:
                    return 礦山區;
                case Sector.步兵區:
                    return 步兵區;
                case Sector.已完成的奇蹟:
                    return 已完成的奇蹟;
//                
//                
                case Sector.劇院區: // 9
                    return 劇院區;
                case Sector.圖書館區: //10
                    return 圖書館區;
                case Sector.殖民領土區://11
                    return 殖民領土區;
                case Sector.炮兵區://12
                    return 炮兵區;
                case Sector.特殊科技區://13
                    return 特殊科技區;
                case Sector.競技場區://14
                    return 競技場區;
                case Sector.空軍區://15
                    return 空軍區;
                case Sector.騎兵區://16
                    return 騎兵區;
                case Sector.未分類區://17
                    return 未分類區;

                case Sector.內政區://18
                    return 內政區;
                case Sector.軍事區://19
                    return 軍事區;
                case Sector.建築區://20
                    return 建築區;
                case Sector.殖民區://21
                    return 殖民區;
                case Sector.行動牌區://22
                    return 行動牌區;
                case Sector.戰術區://23
                    return 戰術區;
                case Sector.戰爭區://24
                    return 戰爭區;
//            case Sector.建築區://19
//                return 建築區;

                default:
                    return null;
            }
        }

        public List<AgesCard> get炮兵區() {
            return 炮兵區;
        }

        public void set炮兵區(List<AgesCard> 炮兵區) {
            this.炮兵區 = 炮兵區;
        }

        public List<AgesCard> get空軍區() {
            return 空軍區;
        }

        public void set空軍區(List<AgesCard> 空軍區) {
            this.空軍區 = 空軍區;
        }

        public List<AgesCard> get劇院區() {
            return 劇院區;
        }

        public void set劇院區(List<AgesCard> 劇院區) {
            this.劇院區 = 劇院區;
        }

        public List<AgesCard> get圖書館區() {
            return 圖書館區;
        }

        public void set圖書館區(List<AgesCard> 圖書館區) {
            this.圖書館區 = 圖書館區;
        }

        public List<AgesCard> get競技場區() {
            return 競技場區;
        }

        public void set競技場區(List<AgesCard> 競技場區) {
            this.競技場區 = 競技場區;
        }

//        public List<Integer> getWonderStages() {
//            return wonderStages;
//        }
//
//        public void setWonderStages(List<Integer> wonderStages) {
//            this.wonderStages = wonderStages;
//        }
        public List<AgesCard> get行動牌區() {
            return 行動牌區;
        }

        public void set行動牌區(List<AgesCard> 行動牌區) {
            this.行動牌區 = 行動牌區;
        }

        public List<AgesCard> get農場區() {
            return 農場區;
        }

        public List<AgesCard> get礦山區() {
            return 礦山區;
        }

        public List<AgesCard> get步兵區() {
            return 步兵區;
        }

        public List<AgesCard> get內政區() {
            return 內政區;
        }

        public void set內政區(List<AgesCard> 內政區) {
            this.內政區 = 內政區;
        }

        public List<AgesCard> get軍事區() {
            return 軍事區;
        }

        public void set軍事區(List<AgesCard> 軍事區) {
            this.軍事區 = 軍事區;
        }

        public List<AgesCard> get建築區() {
            return 建築區;
        }

        public void set建築區(List<AgesCard> 建築區) {
            this.建築區 = 建築區;
        }

        public List<AgesCard> get殖民區() {
            return 殖民區;
        }

        public void set殖民區(List<AgesCard> 殖民區) {
            this.殖民區 = 殖民區;
        }

        public void subAddCardToSector(AgesCard card, List<AgesCard> list) {
            list.add(card);
        }

        public void subMove卡牌列CardToSector(int index, AgesCard card, List<AgesCard> list) {
            list.add(card);
            sub在卡牌列拿掉指定位子的卡牌and補上一張空卡牌(index);
            card.set拿牌回合(round.getVal());

        }

        public List<AgesCard> get建造中的奇蹟區() {
            return 建造中的奇蹟區;
        }

        public List<AgesCard> get已完成的奇蹟() {
            return 已完成的奇蹟;
        }

        public List<AgesCard> get殖民領土區() {
            return 殖民領土區;
        }

        public List<AgesCard> get特殊科技區() {
            return 特殊科技區;
        }

        public List<AgesCard> get手牌內政牌區() {
            return 手牌內政牌區;
        }

        public List<AgesCard> get行動牌暫存區() {
            return 行動牌區;
        }

        public List<AgesCard> get手牌軍事牌區() {
            return 手牌軍事牌區;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Player(String str) {
            name = str;
            init();
        }

        public boolean act升級(int id1, int id2) {
            if (is升級實驗室神廟(id1, id2)) {
                showDebug("TARGET ID IS IN 實驗室神廟");
                return act升級實驗室神廟(id1, id2);
            }

            showDebug("TARGET ID IS NOT IN 實驗室神廟");

            return false;
//            this.actBuild奇蹟(id);
        }

        public boolean act建造(int id) {
            if (isBuild實驗室神廟(id)) {
                showDebug("TARGET ID IS IN 實驗室神廟");
                return act建造實驗室神廟(id);
            }
            showDebug("TARGET ID IS NOT IN 實驗室神廟");
            if (isBuild農場礦山(id)) {
                showDebug("TARGET ID IS IN 農場礦山");
                return act建造農場礦山(id);
            }
            showDebug("TARGET ID IS NOT IN 農場礦山");

            if (isBuild奇蹟(id)) {
                showDebug("TARGET ID IS IN 奇蹟");
                return actBuild奇蹟();
            }
            showDebug("TARGET ID IS NOT IN 奇蹟");
            if (isBuild步兵(id)) {
                showDebug("TARGET ID IS IN 步兵");
                return act建造步兵(id);
            }
            return false;
//            this.actBuild奇蹟(id);
        }

        public void actDestroy(int id) {
            this.actDestroy農場礦山(id);

        }

        /**
         * Caller's responsibility to ensure there is one and only one card on
         * 建造中的奇蹟區
         *
         * @param id
         * @return
         */
        public boolean actBuild奇蹟() {
//2014-5-19-max 針對奇蹟的部分
//            for (AgesCard card : 建造中的奇蹟區) {
            AgesCard card = 建造中的奇蹟區.get(0);

//            if (card.getId() != id) {
//                showWhyNoAction(ReasonWhyNoAction.NOT_THE_奇蹟CARD_UNDER_CONSTRUCTION);
//                return false;
//            }
            if (currentPlayer.目前內政點數.getVal() < 1) {
                showWhyNoAction(ReasonWhyNoAction.內政點數不够);
                return false;
            }

//            int required資源 = wonderStages.get(0).intValue();
            int required資源 = getRequired資源to建造奇蹟();
            if (currentPlayer.getAvailable資源() < required資源) {
                showWhyNoAction(ReasonWhyNoAction.資源不够);
                showDebug(" REQUIRED: " + required資源);
                showDebug(" YOU ONLY HAVE 資源 " + currentPlayer.getAvailable資源());
                return false;
            }

            sub支付資源(required資源);
            subMove資源庫藍點to卡牌(card, 1);
            sub支付內政點數(1);
            showDoneDescription(DoneDescription.BUILD_ONE_STAGE_WONDER);
            if (getHowManyMoreStage建造奇蹟() == 0) {
//                System.out.println("going to move " + card.getTokenBlue() + "藍點From卡牌To資源庫");
//                    for (int k = 0; k < card.getTokenBlue(); k++) {
                subMove卡牌藍點to資源庫(card, card.getTokenBlue());
//                    }

                moveOneCard(建造中的奇蹟區, 0, 已完成的奇蹟);
                showDoneDescription(DoneDescription.COMPLETE_ONE_WONDER);

//                System.out.println("TRANSFER THIS WONDER FROM 建造中的奇蹟區 TO 已完成的奇蹟 ");
//                int temp = 內政手牌上限.getVal();
                subUpdate手牌上限();
                if (card.getName().equals("金字塔")) {
                    sub支付內政點數(-1);//
                    showDebug("JUST FINISH 金字塔, TAKE ONE MORE WHITE TOKEN INTO THE GAME");
                }
//                    
//                if (內政手牌上限.getVal() != temp) {
//                    System.out.println("************* 內政手牌上限 HAS BEEN CHANGED , SHOULD WE ADD 內政點數 TOKEN NOW?????");
//                    System.out.println("max-2014-5-19，以金字塔為例建造完成後立即生效行動:獲得一點內政行動");
//                    System.out.println("手牌上限值要+1");
//                    System.out.println("內政點數也要+1，如同在遊戲盒裡拿出一個白色標記");
//                }
            }
//            System.out.println("done, build 奇蹟 ONE STAGE ");
            return true;//一次只操作一張牌，找到後返回

        }

//        private void subMove卡牌藍點to資源庫(AgesCard card) {
//
////            card.setTokenBlue(card.getTokenBlue() - 1);
////            get資源庫_藍點().addPoints(+1);
////            System.out.println("move藍點From卡牌To資源庫");
//        }
        private void subMove卡牌藍點to資源庫(AgesCard card, int amount) {
            card.setTokenBlue(card.getTokenBlue() - amount);
            get資源庫_藍點().addPoints(+amount);
//            System.out.println("move " + amount + "藍點 From卡牌To資源庫");
        }

        private boolean subMove資源庫藍點to卡牌(AgesCard card, int amount) {
            if (get資源庫_藍點().getVal() > 0) {
                showDebug(" before " + card.toString(STYLE_普通_藍點));
                showDebug(" before 資源庫_藍點=" + get資源庫_藍點().getVal());

                card.setTokenBlue(card.getTokenBlue() + amount);
                get資源庫_藍點().addPoints(-amount);

                showDebug(" after " + card.toString(STYLE_普通_藍點));
                showDebug(" after 資源庫_藍點=" + get資源庫_藍點().getVal());
                return true;
            } else {
                return false;
            }
//            System.out.println("move藍點From資源庫To卡牌" + card.toString(STYLE_普通));
        }

        public boolean isBuild實驗室神廟(int id) {
            List<AgesCard> buildList = new ArrayList<>();
            buildList.addAll(實驗室);
            buildList.addAll(神廟區);
            for (AgesCard card : buildList) {
                if (card.getId() == id) {//找到目標的牌
                    return true;
                }
            }
            return false;
        }

        public boolean is升級實驗室神廟(int id1, int id2) {
            List<AgesCard> buildList = new ArrayList<>();
            buildList.addAll(實驗室);
            buildList.addAll(神廟區);
            for (AgesCard card1 : buildList) {
                System.out.println("開始找第一張牌");
                if (card1.getId() == id1) {//找到目標的牌
                    System.out.println("找到第一張牌" + id1);
                    for (AgesCard card2 : buildList) {
                        System.out.println("開始找第二張牌");
                        if (card2.getId() == id2) {
                            System.out.println("找到第二張牌" + id2);
                            return true;
                        }

                    }
                    return false;
                }
            }
            return false;
        }

        public boolean isBuild農場礦山(int id) {
            List<AgesCard> buildList = new ArrayList<>();
            buildList.addAll(農場區);
            buildList.addAll(礦山區);
            for (AgesCard card : buildList) {
                if (card.getId() == id) {//找到目標的牌
                    return true;
                }
            }
            return false;
        }

        public boolean isBuild步兵(int id) {
            List<AgesCard> buildList = new ArrayList<>();
            buildList.addAll(步兵區);
            for (AgesCard card : buildList) {
                if (card.getId() == id) {//找到目標的牌
                    return true;
                }
            }
//            showWhyNoAction(ReasonWhyNoAction.NOT_THE_奇蹟CARD_UNDER_CONSTRUCTION);
            return false;
        }

        public boolean isBuild奇蹟(int id) {
            List<AgesCard> buildList = new ArrayList<>();
            buildList.addAll(建造中的奇蹟區);
            for (AgesCard card : buildList) {
                if (card.getId() == id) {//找到目標的牌
                    return true;
                }
            }
//            showWhyNoAction(ReasonWhyNoAction.NOT_THE_奇蹟CARD_UNDER_CONSTRUCTION);
            return false;
        }
//實驗室神廟

        public AgesCard 置入卡牌(int id) {
            List<AgesCard> buildList = new ArrayList<>();
            buildList.addAll(實驗室);
            buildList.addAll(神廟區);
            for (AgesCard card : buildList) {
                if (card.getId() == id) {//找到目標的牌
                    return card;
                }
            }
            return NOCARD_HERE;
        }

        public AgesCard chk實驗室神廟(int id) {
            List<AgesCard> buildList = new ArrayList<>();
            int required內政點數 = 1;
            int required資源 = 999;

            buildList.addAll(實驗室);
            buildList.addAll(神廟區);
            for (AgesCard card : buildList) {
                if (card.getId() == id) {//找到目標的牌
                    if (currentPlayer.目前內政點數.getVal() < required內政點數) {
//                        System.out.println("no action, 你沒有內政點數");
                        showWhyNoAction(ReasonWhyNoAction.內政點數不够);
                        break;
                    }

                    required資源 = card.getCostStone();

                    if (currentPlayer.getAvailable資源() < required資源) {
//                        System.out.println("no action, 你沒有足夠的資源. available資源" + currentPlayer.getAvailable資源() + "< requried資源" + card.getCostStone());
                        showWhyNoAction(ReasonWhyNoAction.資源不够);
                        break;
                    }
                    if (currentPlayer.工人區_黃點.getVal() < 1) {
//                        System.out.println("no action, 你的工人區【黃】沒人");
                        showWhyNoAction(ReasonWhyNoAction.工人區沒人);
                        break;
                    }
                    return card;
                }
            }
            return NOCARD_HERE;

        }

        public AgesCard chk建造農場礦山(int id) {
            List<AgesCard> buildList = new ArrayList<>();
            int required內政點數 = 1;
            int required資源 = 999;

            buildList.addAll(農場區);
            buildList.addAll(礦山區);
            for (AgesCard card : buildList) {
                if (card.getId() == id) {//找到目標的牌
                    if (currentPlayer.目前內政點數.getVal() < required內政點數) {
                        System.out.println("no action, 你沒有內政點數");
                        break;
                    }

                    required資源 = card.getCostStone();

                    if (currentPlayer.getAvailable資源() < required資源) {
                        System.out.println("no action, 你沒有足夠的資源. available資源" + currentPlayer.getAvailable資源() + "< requried資源" + card.getCostStone());
                        break;
                    }
                    if (currentPlayer.工人區_黃點.getVal() < 1) {
                        System.out.println("no action, 你的工人區【黃】沒人");
                        break;
                    }
                    return card;
                }
            }
            return NOCARD_HERE;

        }

        public AgesCard chk建造步兵(int id) {
            List<AgesCard> buildList = new ArrayList<>();
            int required軍事點數 = 1;
            int required資源 = 999;

            buildList.addAll(步兵區);
//            buildList.addAll(礦山區);
            for (AgesCard card : buildList) {
                if (card.getId() == id) {//找到目標的牌
                    if (currentPlayer.目前軍事點數.getVal() < required軍事點數) {
                        System.out.println("no action, 你沒有軍事點數");
                        break;
                    }

                    required資源 = card.getCostStone();

                    if (currentPlayer.getAvailable資源() < required資源) {
                        System.out.println("no action, 你沒有足夠的資源. available資源" + currentPlayer.getAvailable資源() + "< requried資源" + card.getCostStone());
                        break;
                    }
                    if (currentPlayer.工人區_黃點.getVal() < 1) {
                        System.out.println("no action, 你的工人區【黃】沒人");
                        break;
                    }
                    return card;
                }
            }
            return NOCARD_HERE;

        }

        public boolean act升級實驗室神廟(int id1, int id2) {
            AgesCard card1 = 置入卡牌(id1);
            AgesCard card2 = 置入卡牌(id2);
            if (目前內政點數.getVal() < 1) {
                showWhyNoAction(ReasonWhyNoAction.內政點數不够);
                return false;
            }
            int required資源 = card2.getCostStone() - card1.getCostStone();
            if (getAvailable資源() < required資源) {
                showWhyNoAction(ReasonWhyNoAction.資源不够);
                showDebug(" REQUIRED: " + required資源);
                showDebug(" YOU ONLY HAVE 資源 " + currentPlayer.getAvailable資源());
                return false;
            }
            if (card1.getTokenYellow() == 0) {
                showWhyNoAction(ReasonWhyNoAction.升級位置沒有黃點);
                return false;
            }
            if (card1.getTag() != card2.getTag()) {
                showWhyNoAction(ReasonWhyNoAction.升級位置與目標不一致);
                return false;
            }
            if (card1.getAge() >= card2.getAge()) {
                showWhyNoAction(ReasonWhyNoAction.升級目標必須比升級位置的時代高);
            }

            sub升級實驗室神廟(card1, card2);
            return true;

        }

        //實驗室神廟
        public boolean act建造實驗室神廟(int id) {
            AgesCard card = chk實驗室神廟(id);

            if (card.isNOCARD()) {
                return false;
            }

            int currentBldgCnt = card.getTokenYellow();
            int maxBldgCnt = get建築上限().getVal();
            if (currentBldgCnt == maxBldgCnt) {
                showWhyNoAction(ReasonWhyNoAction.ALREADY_ON_MAX_BLDG_CNT);
                return false;
            }

            if (目前內政點數.getVal() < 1) {
                showWhyNoAction(ReasonWhyNoAction.內政點數不够);
                return false;
            }

//            int required資源 = wonderStages.get(0).intValue();
            int required資源 = card.getCostStone();
            if (getAvailable資源() < required資源) {
                showWhyNoAction(ReasonWhyNoAction.資源不够);
                showDebug(" REQUIRED: " + required資源);
                showDebug(" YOU ONLY HAVE 資源 " + currentPlayer.getAvailable資源());
                return false;
            }

            if (工人區_黃點.getVal() < 1) {
                showWhyNoAction(ReasonWhyNoAction.工人區沒人);
                return false;
            }

            sub建造實驗室神廟(card);
            return true;

        }

        public boolean act建造農場礦山(int id) {
            AgesCard card = chk建造農場礦山(id);
            if (card.isNOCARD()) {
                return false;
            }
            sub建造農場礦山(card);
            return true;

        }

        public void subMove工人區黃點to卡牌(AgesCard card) {
            工人區_黃點.addPoints(-1);//玩家的工人區-1
            card.setTokenYellow(card.getTokenYellow() + 1);//指定的卡上黃點+1

        }

//        
        public void subMove人力庫黃點to工人區() {
            showDebug("subMove人力庫黃點to工人區() before 工人區_黃點=" + 工人區_黃點.getVal() + " 人力庫_黃點" + 人力庫_黃點.getVal());
            工人區_黃點.addPoints(1);//
            人力庫_黃點.addPoints(-1);
            showDebug("subMove人力庫黃點to工人區()  after 工人區_黃點=" + 工人區_黃點.getVal() + " 人力庫_黃點" + 人力庫_黃點.getVal());

        }

        public void subMove卡牌黃點to工人區(AgesCard card) {
            工人區_黃點.addPoints(1);//玩家的工人區-1
            card.setTokenYellow(card.getTokenYellow() - 1);//指定的卡上黃點+1

        }

        public void subMove卡牌黃點to卡牌(AgesCard cardFrom, AgesCard cardTo) {
            cardFrom.setTokenYellow(cardFrom.getTokenYellow() - 1);//指定的卡上黃點+1
            cardTo.setTokenYellow(cardTo.getTokenYellow() + 1);//指定的卡上黃點+1

        }

        public void sub建造實驗室神廟(AgesCard card) {

            // 1.支付1點內政點數
            sub支付內政點數(1);

            // 2.支付指定數量的资源    
//            subMove卡牌藍點to資源庫(card, card.getCostStone());
            sub支付資源(card.getCostStone());

            // 3.黃點
            subMove工人區黃點to卡牌(card);
//            sub
            sub更新文明板塊上所提供的數據();

        }

        public void sub升級實驗室神廟(AgesCard card1, AgesCard card2) {

            // 1.支付1點內政點數
            sub支付內政點數(1);
            // 2.支付指定數量的资源    
//            subMove卡牌藍點to資源庫(card, card.getCostStone());
            sub支付資源(card2.getCostStone() - card1.getCostStone());

            // 3.黃點
            this.subMove卡牌黃點to卡牌(card1, card2);
//            sub
            sub更新文明板塊上所提供的數據();

        }

        public void sub建造步兵(AgesCard card) {

            // 1.支付1點內政點數
            sub支付軍事點數(1);

            // 2.支付指定數量的资源    
//            subMove卡牌藍點to資源庫(card, card.getCostStone());
            sub支付資源(card.getCostStone());

            // 3.黃點
            subMove工人區黃點to卡牌(card);
            sub更新文明板塊上所提供的數據();
        }

        public void sub建造農場礦山(AgesCard card) {

            // 1.支付1點內政點數
            sub支付內政點數(1);

            // 2.支付指定數量的资源    
//            subMove卡牌藍點to資源庫(card, card.getCostStone());
            sub支付資源(card.getCostStone());

            // 3.黃點
            subMove工人區黃點to卡牌(card);
            sub更新文明板塊上所提供的數據();
        }

        public boolean actDestroy農場礦山(int id) {
            List<AgesCard> buildList = new ArrayList<>();
            buildList.addAll(農場區);
            buildList.addAll(礦山區);
            for (AgesCard card : buildList) {
                if (card.getId() == id) {//找到目標的牌
                    if (currentPlayer.目前內政點數.getVal() < 1) {
                        System.out.println("你沒有足夠的內政點數");
                        return false;
                    }
                    if (card.getTokenYellow() < 1) {
                        System.out.println("你沒有工人");
                        return false;
                    }
//                    System.out.println("before  內政點數:" + this.內政點數 + "  工人區:" + this.工人區_黃點 + "  這張牌的黃點:"
//                            + +card.getTokenYellow() + "  (成本" + card.getIconPoints() + ")" + "  礦山區 A青銅1032 藍點:" + this.get礦山區().get(0).getTokenBlue() + "  資源庫【藍】:" + this.資源庫_藍點);
                    card.setTokenYellow(card.getTokenYellow() - 1);//指定的卡上黃點+1
                    this.工人區_黃點.addPoints(+1);//玩家的工人區-1
                    this.目前內政點數.addPoints(-1);
                    //支付石頭
//                    this.礦山區.get(0).setTokenBlue(礦山區.get(0).getTokenBlue() - card.getCostStone());
                    //增加資源庫的藍點
//                    this.資源庫_藍點.addPoints(card.getCostStone());
//                    System.out.println("after  內政點數:" + this.內政點數 + "  工人區:" + this.工人區_黃點 + "  這張牌的黃點:"
//                            + +card.getTokenYellow() + "  (成本" + card.getIconPoints() + ")" + "  礦山區 A青銅1032 藍點:" + this.get礦山區().get(0).getTokenBlue() + "  資源庫【藍】:" + this.資源庫_藍點);
                    System.out.println("摧毀成功" + card.getName());
                    return true;//一次只操作一張牌，找到後返回
                }
            }
//            System.out.println("不在可建造的項目裡面,目前只能建造農場、礦山");
            System.out.println("指定牌不為農場礦山");
            return false;
        }

        public void actAct(int id) {
//2014-5-19-max 針對實驗室、神廟區、農場區、礦山區、步兵區
            List<AgesCard> actbuildList = new ArrayList<>();
            actbuildList.addAll(實驗室);
            actbuildList.addAll(神廟區);
            actbuildList.addAll(農場區);
            actbuildList.addAll(礦山區);
            actbuildList.addAll(步兵區);
            System.out.println(get行動牌暫存區().get(0).getName() + "  效果:" + get行動牌暫存區().get(0).getAction());
            for (AgesCard card : actbuildList) {
                if (card.getId() == id) {
                    System.out.println("目標卡名:" + card.getName());

                    switch (get行動牌暫存區().get(0).getId()) {
                        //富饒之土
                        case 1013:
                        case 1061:
                        case 1025:
                            System.out.println("處理富饒之土");
                            if ((card.getTag().equals("農場")) || (card.getTag().equals("礦山"))) {
                                System.out.println("正確的對象");
                                if (get行動牌暫存區().get(0).getAge() + 1 > card.getCostStone()) {//如果減少>花費
                                    card.setTokenYellow(card.getTokenYellow() + 1);//黃點+1
                                    this.工人區_黃點.addPoints(-1);
                                } else {
                                    System.out.println("此次花費:" + (card.getCostStone() - (get行動牌暫存區().get(0).getAge() + 1)));
                                    this.礦山區.get(0).setTokenBlue(礦山區.get(0).getTokenBlue() - (card.getCostStone() - (get行動牌暫存區().get(0).getAge() + 1)));//支付成本
                                    card.setTokenYellow(card.getTokenYellow() + 1);//黃點+1
                                    this.工人區_黃點.addPoints(-1);
                                }
                                get行動牌暫存區().remove(0);
                            } else {
                                System.out.println("錯誤的目標");
                            }
                            break;
                        //                        建築工地
                        case 1017:
                        case 1065:
                        case 1132:
                        case 1215:
                            System.out.println("處理建築工地");
                            switch (card.getTag()) {
                                case "神廟":
                                case "實驗室":
                                    System.out.println("正確的對象");
                                    if (get行動牌暫存區().get(0).getAge() + 1 > card.getCostStone()) {//如果減少>花費
                                        card.setTokenYellow(card.getTokenYellow() + 1);//黃點+1
                                        this.工人區_黃點.addPoints(-1);
                                    } else {
                                        System.out.println("此次花費:" + (card.getCostStone() - (get行動牌暫存區().get(0).getAge() + 1)));
                                        this.礦山區.get(0).setTokenBlue(礦山區.get(0).getTokenBlue() - (card.getCostStone() - (get行動牌暫存區().get(0).getAge() + 1)));//支付成本
                                        card.setTokenYellow(card.getTokenYellow() + 1);//黃點+1
                                        this.工人區_黃點.addPoints(-1);
                                    }
                                    get行動牌暫存區().remove(0);
                                    break;

                                default:
                                    System.out.println("錯誤的目標");
                                    break;
                            }

                        default:
                            System.out.println("待處理中");
                            break;
                    }

                }
//            System.out.println("your assigned ID " + id + " IS NOT FOUND???");
            }
        }

        public void act摧毀(int id) {
//            System.out.println("這是在player裡面的");
//            this.農場區.get(0).setTokenYellow(this.農場區.get(0).getTokenYellow() + 1);
            /* OLD STYLE, ONE BY ONE
             for (AgesCard card : 農場區) {
             if (card.getId() == id) {
             card.setTokenYellow(card.getTokenYellow() - 1);
             this.工人區_黃點.addPoints(1);
             //                    System.out.println(" " + key + " " + getSameSizeName(card.getName()) + " " + card.getAction());
             }
             }
             for (AgesCard card : 礦山區) {
             if (card.getId() == id) {
             card.setTokenYellow(card.getTokenYellow() - 1);
             this.工人區_黃點.addPoints(1);
             //                    System.out.println(" " + key + " " + getSameSizeName(card.getName()) + " " + card.getAction());
             }
             }
             */

            List<AgesCard> buildList = new ArrayList<>();
            buildList.addAll(實驗室);
            buildList.addAll(神廟區);
            buildList.addAll(農場區);
            buildList.addAll(礦山區);
            buildList.addAll(步兵區);
            for (AgesCard card : buildList) {
                if (card.getId() == id) {
                    card.setTokenYellow(card.getTokenYellow() - 1);
                    this.工人區_黃點.addPoints(+1);
                    return;
                }
            }

        }

        public boolean do擴充人口() {
            if (isOk擴充人口()) {
                sub擴充人口();
                return true;
            }
            return false;
        }

        public void sub擴充人口() {
            showDebug("DOING...sub擴充人口");
//            System.out.println("現在要擴充人口了 1. Pay one civil point 2.(TODO)PAY FOOD 3.move Yellow Token");
//            this.農場區.get(0).setTokenYellow(this.農場區.get(0).getTokenYellow() + 1);
//            this.人力庫_黃點.addPoints(-1);
//            this.工人區_黃點.addPoints(1);
            sub支付內政點數(1);
//            System.out.println("(TODO)PAY FOOD");
            int amt = get擴充人口需要支付食物();
            showDebug("DOING...sub擴充人口  going sub支付食物" + amt);
            sub支付食物(amt);
            subMove人力庫黃點to工人區();

        }

        private String name;
        private Points 目前內政點數;
        private Points 目前軍事點數;
        private Points 建築上限;
        private Points 內政點數上限;
        private Points 軍事點數上限;
        private Points 殖民點數;
        private Points 文化;
        private Points 文化生產_當回合;
        private Points 科技;
        private Points 科技生產_當回合;
        private Points 軍力;
        private Points 資源庫_藍點;
        private Points 額外用於建造軍事單位的資源;

        private Points 人力庫_黃點;
        private Points 笑臉_幸福指數;
        private Points 工人區_黃點;

        public void setScore(Score score) {
            this.score = score;
        }

        public void setToken黃(Token token黃) {
            this.token黃 = token黃;
        }

        public void setToken藍(Token token藍) {
            this.token藍 = token藍;
        }

        public void set內政點數(Points 內政點數) {
            this.目前內政點數 = 內政點數;
        }

        public void set軍事點數(Points 軍事點數) {
            this.目前軍事點數 = 軍事點數;
        }

        public void set建築上限(Points 建築上限) {
            this.建築上限 = 建築上限;
        }

        public void set內政手牌上限(Points 內政手牌上限) {
            this.內政點數上限 = 內政手牌上限;
        }

        public void set軍事手牌上限(Points 軍事手牌上限) {
            this.軍事點數上限 = 軍事手牌上限;
        }

        public void set殖民點數(Points 殖民點數) {
            this.殖民點數 = 殖民點數;
        }

        public void set文化(Points 文化) {
            this.文化 = 文化;
        }

        public void set文化生產_當回合(Points 文化生產_當回合) {
            this.文化生產_當回合 = 文化生產_當回合;
        }

        public void set科技(Points 科技) {
            this.科技 = 科技;
        }

        public void set科技生產_當回合(Points 科技生產_當回合) {
            this.科技生產_當回合 = 科技生產_當回合;
        }

        public void set軍力(Points 軍力) {
            this.軍力 = 軍力;
        }

        public void set資源庫_藍點(Points 資源庫_藍點) {
            this.資源庫_藍點 = 資源庫_藍點;
        }

        public void set人力庫_黃點(Points 人力庫_黃點) {
            this.人力庫_黃點 = 人力庫_黃點;
        }

        public void set笑臉_幸福指數(Points 笑臉_幸福指數) {
            this.笑臉_幸福指數 = 笑臉_幸福指數;
        }

        public void set工人區_黃點(Points 工人區_黃點) {
            this.工人區_黃點 = 工人區_黃點;
        }

        public void set領袖區(List<AgesCard> 領袖區) {
            this.領袖區 = 領袖區;
        }

        public void set政府區(List<AgesCard> 政府區) {
            this.政府區 = 政府區;
        }

        public void set實驗室(List<AgesCard> 實驗室) {
            this.實驗室 = 實驗室;
        }

        public void set神廟區(List<AgesCard> 神廟區) {
            this.神廟區 = 神廟區;
        }

        public void set農場區(List<AgesCard> 農場區) {
            this.農場區 = 農場區;
        }

        public boolean is已有該張科技牌(AgesCard card12345) {

            for (AgesCard card : get科技牌的檢查區域()) {
                if (card.getId() == card12345.getId()) {
                    System.out.println("你不能拿取已經有的科技牌");
                    return true;
                }

            }
            return false;
        }

        public List<AgesCard> get科技牌的檢查區域() {
            List<AgesCard> list = new ArrayList<>();
            list.addAll(政府區);
            list.addAll(實驗室);
            list.addAll(劇院區);
            list.addAll(競技場區);
            list.addAll(圖書館區);
            list.addAll(神廟區);
            list.addAll(農場區);
            list.addAll(礦山區);
            list.addAll(步兵區);
            list.addAll(騎兵區);
            list.addAll(炮兵區);
            list.addAll(空軍區);
            list.addAll(手牌內政牌區);
            return list;
        }
        /*
         政府區
         實驗室
         劇院區
         競技場
         圖書館
         神廟區
         農場區
         礦山區
         步兵區
         騎兵區
         炮兵區
         空軍區
         手牌內政牌區
         */

        public void set礦山區(List<AgesCard> 礦山區) {
            this.礦山區 = 礦山區;
        }

        public void set步兵區(List<AgesCard> 步兵區) {
            this.步兵區 = 步兵區;
        }

        public void set建造中的奇蹟區(List<AgesCard> 建造中的奇蹟區) {
            this.建造中的奇蹟區 = 建造中的奇蹟區;
        }

        public void set已完成的奇蹟(List<AgesCard> 已完成的奇蹟) {
            this.已完成的奇蹟 = 已完成的奇蹟;
        }

        public void set殖民領土區(List<AgesCard> 殖民領土區) {
            this.殖民領土區 = 殖民領土區;
        }

        public void set特殊科技區(List<AgesCard> 特殊科技區) {
            this.特殊科技區 = 特殊科技區;
        }

        public void set手牌內政牌區(List<AgesCard> 手牌內政牌區) {
            this.手牌內政牌區 = 手牌內政牌區;
        }

        public void set手牌軍事牌區(List<AgesCard> 手牌軍事牌區) {
            this.手牌軍事牌區 = 手牌軍事牌區;
        }

        private List<AgesCard> 暫存應用區;
        private List<AgesCard> 戰爭區;
        private List<AgesCard> 戰術區;
        private List<AgesCard> 領袖區;
        private List<AgesCard> 政府區;
        private List<AgesCard> 實驗室;
        private List<AgesCard> 神廟區;
        private List<AgesCard> 農場區;
        private List<AgesCard> 礦山區;
        private List<AgesCard> 步兵區;
        private List<AgesCard> 未分類區;
// [09:54:37] maxchen20041: 請按照步兵區方式
// [09:55:19] maxchen20041: 建立騎兵區、炮兵區、空軍區、劇院區、圖書館區、競技場區
        private List<AgesCard> 騎兵區;
        private List<AgesCard> 炮兵區;
        private List<AgesCard> 空軍區;
        private List<AgesCard> 劇院區;
        private List<AgesCard> 圖書館區;
        private List<AgesCard> 競技場區;

        private List<AgesCard> 內政區;
        private List<AgesCard> 軍事區;
        private List<AgesCard> 建築區;
        private List<AgesCard> 殖民區;
        private List<AgesCard> 行動區;
        private List<AgesCard> 建造中的奇蹟區;
//        private List<Integer> wonderStages;

        private List<AgesCard> 已完成的奇蹟;
        private List<AgesCard> 殖民領土區;
        private List<AgesCard> 特殊科技區;
        private List<AgesCard> 手牌內政牌區;
        private List<AgesCard> 行動牌區;
        public List<AgesCard> 手牌軍事牌區;

        public List<AgesCard> get暫存應用區() {
            return 暫存應用區;
        }

        public void set暫存應用區(List<AgesCard> 暫存應用區) {
            this.暫存應用區 = 暫存應用區;
        }

        public void init() {
            //TESTING 
            score = new Score();
            score.getMap().put("內政點數", 0);
            score.getMap().put("軍事點數", 0);
            score.getMap().put("建築上限", 0);
            score.getMap().put("內政手牌上限", 0);
            score.getMap().put("軍事手牌上限", 0);
            score.getMap().put("殖民點數", 0);
            score.getMap().put("文化", 0);
            score.getMap().put("文化﹝＋﹞", 0);
            score.getMap().put("科技", 0);
            score.getMap().put("科技﹝＋﹞", 0);
            score.getMap().put("軍力", 0);
            score.getMap().put("笑臉", 0);
            this.token藍 = new Token();
            this.token黃 = new Token();
            token黃.getMap().put(1010, 1);
            token黃.getMap().put(1007, 2);
            token黃.getMap().put(1032, 2);
            token黃.getMap().put(1018, 1);
            token黃.getMap().put(2, 1);
            token黃.getMap().put(1, 18);
            token藍.getMap().put(3, 18);

            //
            目前內政點數 = new Points();
            目前軍事點數 = new Points();
            內政點數上限 = new Points();
            軍事點數上限 = new Points();
            建築上限 = new Points();
            殖民點數 = new Points();

            文化 = new Points();
            文化生產_當回合 = new Points();//
            科技 = new Points();
            科技生產_當回合 = new Points();
            軍力 = new Points();
            資源庫_藍點 = new Points();
            人力庫_黃點 = new Points();
            笑臉_幸福指數 = new Points();
            工人區_黃點 = new Points();

            戰爭區 = new ArrayList<>();
            戰術區 = new ArrayList<>();
            領袖區 = new ArrayList<>();
            政府區 = new ArrayList<>();
            實驗室 = new ArrayList<>();
            神廟區 = new ArrayList<>();
            農場區 = new ArrayList<>();
            礦山區 = new ArrayList<>();
            步兵區 = new ArrayList<>();
            未分類區 = new ArrayList<>();
// [09:54:37] maxchen20041: 請按照步兵區方式
// [09:55:19] maxchen20041: 建立 騎兵區、炮兵區、空軍區、劇院區、圖書館區、競技場區            
            騎兵區 = new ArrayList<>();
            炮兵區 = new ArrayList<>();
            空軍區 = new ArrayList<>();
            劇院區 = new ArrayList<>();
            圖書館區 = new ArrayList<>();
            競技場區 = new ArrayList<>();

            建造中的奇蹟區 = new ArrayList<>();
//            wonderStages = new ArrayList<>();

            已完成的奇蹟 = new ArrayList<>();
            殖民領土區 = new ArrayList<>();
            特殊科技區 = new ArrayList<>();
            手牌內政牌區 = new ArrayList<>();
            行動牌區 = new ArrayList<>();
            手牌軍事牌區 = new ArrayList<>();

            內政區 = new ArrayList<>();
            軍事區 = new ArrayList<>();
            建築區 = new ArrayList<>();
            殖民區 = new ArrayList<>();

        }

        public void subUpdate手牌上限() {
//        int new回合內政點數 = 政府區.get(0).getEffectWhite();
//        int new回合軍事點數 = 政府區.get(0).getEffectRed();
////            System.out.println(""+政府區.get(0));
//        if (領袖區.size() == 1) {
//            AgesCard leader = 領袖區.get(0);
//            if (leader.getId() == 1023) {// A漢摩拉比1023-內政行動CA+1 軍事行動MA-1】
//                new回合內政點數++;
//                new回合軍事點數--;
//            }
//            if (leader.getId() == 1009) { //【A凱薩1009-軍事力量+1 軍事行動 MA+1】
////                    new回合內政點數++;
//                new回合軍事點數++;
//            }
//        }
//        內政手牌上限.setVal(new回合內政點數);
//        軍事手牌上限.setVal(new回合軍事點數);
            int white = 0;
            int red = 0;
            int house = 0;
            List<AgesCard> buildList = new ArrayList<>();
            buildList.addAll(政府區);
            buildList.addAll(領袖區);
            buildList.addAll(已完成的奇蹟);
            for (AgesCard card : buildList) {
                if (card.getEffectWhite() != 0) {
                    white += card.getEffectWhite();
                }
                if (card.getEffectRed() != 0) {
                    red += card.getEffectRed();
                }
                if (card.getEffectHouse() != 0) {
                    house += card.getEffectHouse();
                }

            }
            int old內政手牌上限 = 內政點數上限.getVal();
            int old軍事手牌上限 = 軍事點數上限.getVal();
            int old建築上限 = 建築上限.getVal();

            if (old內政手牌上限 != white) {
                內政點數上限.setVal(white);
                showDoneDescription(DoneDescription.內政手牌上限_UPDATED);
                showDebug(old內政手牌上限 + " => " + white);
            }
            if (old軍事手牌上限 != red) {
                軍事點數上限.setVal(red);
                showDoneDescription(DoneDescription.軍事手牌上限_UPDATED);
                showDebug(old軍事手牌上限 + " => " + red);
            }
            if (old建築上限 != house) {
                建築上限.setVal(house);
                showDoneDescription(DoneDescription.建築上限_UPDATED);
                showDebug(old建築上限 + " => " + house);
            }
//        System.out.println("內政手牌上限，軍事手牌上限剛剛更新");
        }

//        public void show建造中的奇蹟區Stages() {
//
//            System.out.print("  建造中的奇蹟區Stages ");
//            for (Integer stage : wonderStages) {
//                System.out.print(" " + stage);
//            }
//        }
        public void showNewLine() {
            System.out.println("");
        }

        private void showSectorStyle(List<AgesCard> list, String title, int cardStyle) {
            StringBuilder sb = new StringBuilder();
//            sb.append("\n");
            sb.append(FULLWIDTH_SPACE);
            sb.append(title);
            sb.append(getCardsWithGivenStyle(list, cardStyle));
            System.out.print(sb.toString());
        }

        private String getCardsWithGivenStyle(List<AgesCard> list, int style) {
            StringBuilder sb = new StringBuilder();
            for (AgesCard card : list) {
                sb.append(card.toString(style));
            }
            return sb.toString();
        }

        public void showSector(List<AgesCard> list, String title) {

            switch (title) {

                case "政府區":
                    showNewLine();
                    showSectorStyle(list, title, STYLE_政府區);
                    break;

                case "戰術區":
                case "領袖區":
                    showSectorStyle(list, title, STYLE_領袖區);
                    break;
                case "建造中的奇蹟區":
                    showSectorStyle(list, title, STYLE_建造中的奇蹟區);

                    break;
                case "已完成的奇蹟":
//                    System.out.print("\n  " + title + " ");
//                    for (AgesCard card : list) {
//                        System.out.print("" + card.toString(103));
//                    }
//                    showNewLine();
                    showSectorStyle(list, title, 103);
                    break;

                case "劇院區":
                case "競技場":
                case "圖書館":
                case "騎兵區":
                case "炮兵區":
                case "空軍區":
                    showSectorStyle(list, title, 104);

                    break;

                case "實驗室":
                case "神廟區":
                case "步兵區":
                    showNewLine();
                    showSectorStyle(list, title, STYLE_普通_黃點);
                    break;

                case "農場區":
                case "礦山區":

                    showNewLine();
                    showSectorStyle(list, title, STYLE_實驗室);
                    break;

                case "行動牌區":
//
//                    System.out.println("  ");
//                    System.out.print("" + title + " ");
//                    int j = 0;
//                    for (AgesCard card : list) {
//                        System.out.print("" + (j++) + card.toString(105));
//                    }
                    showSectorStyle(list, title, 105);

                    break;
                case "手牌內政牌區":
//                    System.out.println("  ");
//                    System.out.print("" + title + " ");
//                    int k = 0;
//                    for (AgesCard card : list) {
//                        System.out.print("" + (k++) + card.toString(4));
//                    }
                    showNewLine();

                    showSectorStyle(list, title, 4);

                    break;
                case "手牌軍事牌區":
//                    System.out.println("  ");
//                    System.out.print("" + title + " ");
//                    int p = 0;
//                    for (AgesCard card : list) {
//                        System.out.print("" + (p++) + card.toString(5));
//                    }
                    showNewLine();
                    showSectorStyle(list, title, 5);

                    break;
                case "戰爭區":
//                    System.out.print("  " + title + " ");
//                    for (AgesCard card : list) {
//                        System.out.print("" + card.toString(101));
//                    }
                    showSectorStyle(list, title, 101);

                    break;
                case "未分類":
                    showSectorStyle(list, title, STYLE_普通);
                    break;
                default:
//                    System.out.println("");
//                    System.out.print("" + title + " ");
//                    for (AgesCard card : list) {
//                        System.out.print("" + card.toString(4));
//
//                    }
                    showNewLine();
                    showSectorStyle(list, title, 104);

            }

        }

        public void show() {
            System.out.println("\n  === " + name + " ===     資源:" + get資源明細() + "    食物:" + get食物明細() + "   下回合失去的內政點數:" + get下回合失去的內政點數());
            目前內政點數.show("目前內政點數【白】");
            目前軍事點數.show("目前軍事點數【紅】");
            內政點數上限.show("內政點數上限");
            軍事點數上限.show("軍事點數上限");
            建築上限.show("建築上限");
            System.out.println("");

            文化.show("文化");
            文化生產_當回合.show("文化＋");
            科技.show("科技");
            科技生產_當回合.show("科技＋");
            軍力.show("軍力");
            System.out.println("");

            工人區_黃點.show("工人區【黃】");
            人力庫_黃點.show("人力庫【黃】");
            笑臉_幸福指數.show("笑臉");
            資源庫_藍點.show("資源庫【藍】");
            殖民點數.show("殖民點數");
            System.out.println("");

            showSector(政府區, "政府區");
            showSector(領袖區, "領袖區");
            showSector(建造中的奇蹟區, "建造中的奇蹟區");
//            show建造中的奇蹟區Stages();

            showSector(實驗室, "實驗室");
            showSector(劇院區, "劇院區");
            showSector(競技場區, "競技場");
            showSector(圖書館區, "圖書館");
            showSector(已完成的奇蹟, "已完成的奇蹟");
            showSector(未分類區, "未分類");

            showSector(神廟區, "神廟區");
            showSector(農場區, "農場區");
            showSector(礦山區, "礦山區");

            showSector(步兵區, "步兵區");
            showSector(騎兵區, "騎兵區");
            showSector(炮兵區, "炮兵區");
            showSector(空軍區, "空軍區");

            showSector(戰術區, "戰術區");
            showSector(戰爭區, "戰爭區");
            showSector(手牌內政牌區, "手牌內政牌區");
            showSector(行動牌區, "行動牌區");
            showSector(手牌軍事牌區, "手牌軍事牌區");
            System.out.println("");
        }

        public void act玩家丟棄軍事牌(int val) {
            System.out.println("當前玩家棄掉軍事牌  index=" + val);
            this.moveOneCard(this.手牌軍事牌區, val, field.get軍事牌回收區());
//            this.

        }

        public void actPlayCivilCard(int val) {
            AgesCard card = this.手牌內政牌區.get(val);
//        System.out.println(""+card.getCostRevolution());
//            System.out.println("打出這張牌需要花費(" + card.getCostIdea() + ")科技");

            switch (card.getTag()) {
                case "農場":
                    System.out.println("現在打的是農場牌準本要放到農場區");
                    moveOneCard(this.手牌內政牌區, val, this.農場區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "領袖":
                    System.out.println("***REPLACE CURRENT ONE");
                    while (領袖區.size() > 0) {
                        領袖區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.領袖區);
                    break;
                case "戰術":
                    System.out.println("***REPLACE CURRENT ONE");
//                if (this.sub更新文明板塊上所提供的數) {
                    while (戰術區.size() > 0) {
                        戰術區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.戰術區);
                    break;

                case "戰爭":
                    System.out.println("***REPLACE CURRENT ONE");
                    while (戰爭區.size() > 0) {
                        戰爭區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.戰爭區);
                    break;
                case "行動":
                    System.out.println("打行動牌");
                    while (行動牌區.size() > 0) {
                        行動牌區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.行動牌區);
                    break;
                case "礦山":
                    moveOneCard(this.手牌內政牌區, val, this.礦山區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "實驗室":
                    moveOneCard(this.手牌內政牌區, val, this.實驗室);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "神廟":
                    moveOneCard(this.手牌內政牌區, val, this.神廟區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "步兵":
                    moveOneCard(this.手牌內政牌區, val, this.步兵區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "騎兵":
                    moveOneCard(this.手牌內政牌區, val, this.騎兵區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "炮兵":
                    moveOneCard(this.手牌內政牌區, val, this.炮兵區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "空軍":
                    moveOneCard(this.手牌內政牌區, val, this.空軍區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "競技場":
                    moveOneCard(this.手牌內政牌區, val, this.競技場區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "圖書館":
                    moveOneCard(this.手牌內政牌區, val, this.圖書館區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "劇院":
                    moveOneCard(this.手牌內政牌區, val, this.劇院區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "政府":
                    System.out.println("***REPLACE CURRENT ONE");
//                    System.out.print("" + card.toString(1));
                    while (政府區.size() > 0) {
                        政府區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.政府區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;

                case "內政":
                    while (內政區.size() > 0) {
                        內政區.remove(0);
                    }
                    System.out.println("打出內政牌" + card.getId() + card.getName());
                    moveOneCard(this.手牌內政牌區, val, this.內政區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "軍事":
                    while (軍事區.size() > 0) {
                        軍事區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.軍事區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "建築":
                    while (建築區.size() > 0) {
                        建築區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.建築區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "殖民":
                    while (殖民區.size() > 0) {
                        殖民區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.殖民區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;

                default:
//                    System.out.println("");
                    System.out.print("" + card.toString(1));
                    moveOneCard(this.手牌內政牌區, val, this.未分類區);
            }

            //
            // 06/16 13:30, by Mark
            //
            subUpdate手牌上限();
        }

        public void OLD___actPlayCard(int val) {
            if (val > 手牌內政牌區.size() - 1) {
                System.out.println("我無法作出這個動作，我這個位置沒有牌");
                return;
            }
            AgesCard card = this.手牌內政牌區.get(val);
//        System.out.println(""+card.getCostRevolution());
            System.out.println("打出這張牌需要花費(" + card.getCostIdea() + ")科技");
            switch (card.getTag()) {
                case "農場":
                    System.out.println("現在打的是農場牌準本要放到農場區");
                    moveOneCard(this.手牌內政牌區, val, this.農場區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "領袖":
                    System.out.println("***REPLACE CURRENT ONE");
                    while (領袖區.size() > 0) {
                        領袖區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.領袖區);
                    break;
                case "戰術":
                    System.out.println("***REPLACE CURRENT ONE");
                    while (戰術區.size() > 0) {
                        戰術區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.戰術區);
                    break;
                case "戰爭":
                    System.out.println("***REPLACE CURRENT ONE");
                    while (戰爭區.size() > 0) {
                        戰爭區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.戰爭區);
                    break;
                case "行動":
                    System.out.println("打行動牌");
                    while (行動牌區.size() > 0) {
                        行動牌區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.行動牌區);
                    break;
                case "礦山":
                    moveOneCard(this.手牌內政牌區, val, this.礦山區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "實驗室":
                    moveOneCard(this.手牌內政牌區, val, this.實驗室);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "神廟":
                    moveOneCard(this.手牌內政牌區, val, this.神廟區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "步兵":
                    moveOneCard(this.手牌內政牌區, val, this.步兵區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "騎兵":
                    moveOneCard(this.手牌內政牌區, val, this.騎兵區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "炮兵":
                    moveOneCard(this.手牌內政牌區, val, this.炮兵區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "空軍":
                    moveOneCard(this.手牌內政牌區, val, this.空軍區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "競技場":
                    moveOneCard(this.手牌內政牌區, val, this.競技場區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "圖書館":
                    moveOneCard(this.手牌內政牌區, val, this.圖書館區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "劇院":
                    moveOneCard(this.手牌內政牌區, val, this.劇院區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "政府":
                    System.out.println("***REPLACE CURRENT ONE");
//                    System.out.print("" + card.toString(1));
                    while (政府區.size() > 0) {
                        政府區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.政府區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;

                case "內政":
                    while (內政區.size() > 0) {
                        內政區.remove(0);
                    }
                    System.out.println("打出內政牌" + card.getId() + card.getName());
                    moveOneCard(this.手牌內政牌區, val, this.內政區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "軍事":
                    while (軍事區.size() > 0) {
                        軍事區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.軍事區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "建築":
                    while (建築區.size() > 0) {
                        建築區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.建築區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;
                case "殖民":
                    while (殖民區.size() > 0) {
                        殖民區.remove(0);
                    }
                    moveOneCard(this.手牌內政牌區, val, this.殖民區);
                    this.科技.addPoints(-card.getCostIdea());
                    break;

                default:
//                    System.out.println("");
                    System.out.print("" + card.toString(1));
                    moveOneCard(this.手牌內政牌區, val, this.未分類區);
            }

            //
            // 06/16 13:30, by Mark
            //
            subUpdate手牌上限();
        }

        /**
         *
         * @param p1 from
         * @param p2 to
         */
        public void actUpgrade(int p1, int p2) {

            List<AgesCard> buildList = new ArrayList<>();
            buildList.addAll(實驗室);
            buildList.addAll(神廟區);
            buildList.addAll(農場區);
            buildList.addAll(礦山區);
            buildList.addAll(步兵區);
            System.out.println(" checking 實驗室|神廟區|農場區|礦山區|步兵區, how many cards now? " + buildList.size());

            for (AgesCard card : buildList) {
                if (card.getId() == p1) {
                    card.setTokenYellow(card.getTokenYellow() - 1);
                    break;
                }
            }

            for (AgesCard card : buildList) {
                if (card.getId() == p2) {
                    card.setTokenYellow(card.getTokenYellow() + 1);
                    break;
                }
            }

        }

        public void compute當回合文化and科技and軍力() {
            showDebug("compute當回合文化and科技");

            // 1. Prepare source
            List<AgesCard> list = new ArrayList<>();

//        System.out.println("CURRENTLY ONLY CHECK 已完成的奇蹟");
            list.addAll(政府區);
            list.addAll(領袖區);
            list.addAll(實驗室);
            list.addAll(神廟區);
            list.addAll(農場區);
            list.addAll(步兵區);
            list.addAll(已完成的奇蹟);

            // 2. Process
            int 音樂 = 0;
            int 燈泡 = 0;
            int 武器 = 0;
//
//            for (AgesCard card : list) {
//                if (card.getTokenYellow() > 0) {
//
//                    if (card.getEffectMusic() != 0) {
//                        showDebug("check EffectMusic " + card.toString(103));
//
//                        音樂 += card.getEffectMusic();
//                    }
//                    if (card.getEffectIdea() != 0) {
//
//                        showDebug("check EffectIdea " + card.toString(103));
//                        燈泡 += card.getEffectIdea();
//                    }
//                    if (card.getEffectWeapon() != 0) {
//
//                        showDebug("check EffectWeapon " + card.toString(103));
//                        武器 += card.getEffectWeapon();
//                    }
//                }
//
//            }

            // 3. Update result
            int old音樂 = 文化生產_當回合.getVal();
            int old燈泡 = 科技生產_當回合.getVal();
            int old武器 = 軍力.getVal();

            if (old音樂 != 音樂) {
                文化生產_當回合.setVal(音樂);
                showDebug("文化生產_當回合 " + old音樂 + " => " + 音樂);
            }
            if (old燈泡 != 燈泡) {

                科技生產_當回合.setVal(燈泡);
                showDebug("科技生產_當回合 " + old燈泡 + " => " + 燈泡);
            }
            if (old武器 != 武器) {
                軍力.setVal(武器);
                showDebug("軍力 " + old武器 + " => " + 軍力);
            }

        }

        public void refill內政點數軍事點數() {
            if (內政點數上限.getVal() - this.get下回合失去的內政點數() < 0) {
                目前內政點數.setVal(0);
            } else {
                目前內政點數.setVal(內政點數上限.getVal());
            }
            目前軍事點數.setVal(軍事點數上限.getVal());

        }

        public void actActV1() {
            switch (get行動牌暫存區().get(0).getId()) {
                //富饒之土
                case 1027:
                case 1092:
                case 1171:
                case 1243:
                    System.out.println("處理藝術作品");
                    System.out.println(文化);
                    this.文化.addPoints(6 - get行動牌暫存區().get(0).getAge());
                    System.out.println(文化);
                    get行動牌暫存區().remove(0);
                    break;
                default:
                    System.out.println("使用方式不正確");
                    break;
            }
        }

        /**
         * 回傳只傳正數或是0 get文明所需的笑臉()=5 this.笑臉_幸福指數.getVal()7 return 0
         * get文明所需的笑臉()=5 this.笑臉_幸福指數.getVal()5 return 0 get文明所需的笑臉()=5
         * this.笑臉_幸福指數.getVal()3 return 2
         *
         * @return
         */
        public int get不幸福的工人() {
            int val = get文明所需的笑臉() - this.笑臉_幸福指數.getVal();
            if (val < 0) {
                val = 0;
            }
            return val;
        }

        public int get文明所需的笑臉() {
            int val = 0;
            /*
             0黃需要8
             1~2黃需要7
             3~4黃需要6
             5~6黃需要5
             7~8黃需要4
             9~10黃需要3
             11~12黃需要2
             13~16黃需要1
             17以上黃需要0
             */
            System.out.println("人力庫黃點為" + 人力庫_黃點.getVal());
            int[] val2 = {8, 7, 7, 6, 6, 5, 5, 4, 4, 3, 3, 2, 2, 1, 1, 1, 1, 0};
            if (this.人力庫_黃點.getVal() > 16) {
                val = val2[17];
            } else {
                val = val2[this.人力庫_黃點.getVal()];
            }

            return val;
        }

        private int get擴充人口需要支付食物() {
            int remaining = 人力庫_黃點.getVal();
            switch (remaining) {
                case 1:
                case 2:
                case 3:
                case 4:
                    return 7;
                case 5:
                case 6:
                case 7:
                case 8:
                    return 5;
                case 9:
                case 10:
                case 11:
                case 12:
                    return 4;
                case 13:
                case 14:
                case 15:
                case 16:
                    return 3;
                case 17:
                case 18:
                    return 2;
                default:
                    return 999;
            }
        }

        private boolean is足夠的軍事點數(AgesCard card) {
            if (get軍事點數().getVal() < card.getCostRed()) {

                return false;
            }
            return true;
        }

        private boolean is有1點軍事點數() {
            if (get軍事點數().getVal() < 1) {

                return false;
            }
            return true;
        }

        private boolean isOk擴充人口() {
//            System.out.println("TODO isOk擴充人口 1");
//            System.out.println("TODO isOk擴充人口 2");
//            System.out.println("TODO isOk擴充人口 3");

            if (get內政點數().getVal() < 1) {
                showWhyNoAction(ReasonWhyNoAction.內政點數不够);
                return false;
            }
            if (getAvailable食物() < get擴充人口需要支付食物()) {
                showWhyNoAction(ReasonWhyNoAction.食物不够);
                showDebug("getAvailable食物()=" + getAvailable食物());
                showDebug("get擴充人口需要支付食物()=" + get擴充人口需要支付食物());

                return false;
            }
            if (人力庫_黃點.getVal() < 1) {
                showWhyNoAction(ReasonWhyNoAction.工人區沒人);
                return false;

            }
            return true;

        }

        private void do腐敗() {
            int val = this.get資源庫_藍點().getVal();
            int amt = 0;

            if (val <= 9) {
                amt = AgesCommon.array回合腐敗支付資源對照表[val];
                showDebug("回合腐敗失去資源=" + amt);
            }
            showDebug("!!!!!!!!!!!!!!!!!!!!!!!getAvailable資源=" + getAvailable資源());
            showDebug("!!!!!!!!!!!!!!!!!!!!!!!amt=" + amt);
            if (this.getAvailable資源() < amt) {
//                showWhyNoAction(ReasonWhyNoAction.食物不够);

//                showDebug("!!!!!!!!!!!!!!!!!!!!!!!食物不夠準備扣分=" + amt);
                showDebug("!!!!!!!!!!!!!!!!!!!!!!!getAvailable資源=" + getAvailable資源());
                showDebug("!!!!!!!!!!!!!!!!!!!!!!!amt=" + amt);
//                int val2 = (amt - this.getAvailable食物()) * (-4);
                sub支付資源(this.getAvailable資源());
//                this.sub增加文化值(val2);
            } else {
                sub支付資源(amt);
            }
        }
        int 下回合失去的內政點數;

        public void set下回合失去的內政點數(int 下回合失去的內政點數) {
            this.下回合失去的內政點數 = 下回合失去的內政點數;
        }

        private int get下回合失去的內政點數() {
            return 下回合失去的內政點數;
        }

        private boolean act建造步兵(int id) {
            AgesCard card = chk建造步兵(id);
            if (card.isNOCARD()) {
                return false;
            }
            sub建造步兵(card);
            return true;

        }

        /**
         * 當牌放到未來事件時 將會把第0張的現在事件，移到現在發生事件
         * 如果此效應結束之後現在事件為0張，將未來事件洗牌，並依時代0.1.2.3重新整理過後，移至現在事件
         *
         * 模擬如下
         *
         * 現在發生事件 無 當前事件 ABCD
         *
         * 未來事件 無 放入E卡到未來事件
         *
         * 現在發生事件 A 當前事件 BCD 未來事件
         *
         * E 放入F卡到未來事件
         *
         * 現在發生事件 B 當前事件 CD 未來事件 EF 放入G卡到未來事件
         *
         * 現在發生事件 C 當前事件 D 未來事件 EFG
         *
         * 放入H卡到未來事件
         *
         * 現在發生事件 D 當前事件 未來事件 EFGH
         *
         * 洗牌
         *
         * 現在發生事件 D 當前事件 未來事件 FHEG
         *
         * 現在發生事件 D 當前事件 未來事件 F(1)H(3)E(2)G(1)
         *
         * 調整
         *
         * 現在發生事件 D 當前事件 未來事件 F(1)G(1)E(2)H(3)
         *
         * 現在發生事件 D 當前事件 F(1)G(1)E(2)H(3) 未來事件
         *
         * @param card
         */
        private void sub將牌放入未來事件(AgesCard card) {

            field.get未來事件().add(card);
            currentPlayer.sub增加文化值(card.getAge().intValue());//放事件得分數
//            System.out.println("依照牌的時代給予" + card.getAge() + "分");

            if (field.get現在發生事件().size() != 0) {
                field.get現在發生事件().remove(0);
            }
            moveOneCard(field.get當前事件(), 0, field.get現在發生事件());
            if (field.get當前事件().size() == 0) {
                Collections.shuffle(field.get未來事件());
                for (int j = 0; j < 4; j++) {
                    for (int k = 0; k < 3; k++) {
                        if (field.get未來事件().get(k).getAge() > field.get未來事件().get(k + 1).getAge()) {
                            moveOneCard(field.get未來事件(), k, field.get未來事件());
                            k--;
                        }
                    }
                }
                moveOneCard(field.get未來事件(), 0, field.get當前事件());
                moveOneCard(field.get未來事件(), 0, field.get當前事件());
                moveOneCard(field.get未來事件(), 0, field.get當前事件());
                moveOneCard(field.get未來事件(), 0, field.get當前事件());
            }
        }

        private void sub玩家政治階段打軍事牌(AgesCard card) {
            switch (card.getTag()) {
//            在政治階段，將事件、領土，放入未來事件
                case "事件":
                case "領土":
                    System.out.println("將Tag= 事件 or 領土 牌放入未來事件");
                    this.sub將牌放入未來事件(card);
                    break;
                default:
                    System.out.println("現在只針對事件卡");
            }
        }

        private void sub玩家內政階段打軍事牌(int val) {

            AgesCard card = currentPlayer.手牌軍事牌區.get(val);
            System.out.println("sub玩家內政打軍事牌" + card.getName());
//            玩家政治階段
            switch (card.getTag()) {
//            在政治階段，將事件、領土，放入未來事件
                case "戰術":
                    this.moveOneCard(this.手牌軍事牌區, val, this.戰術區);
                    this.sub支付軍事點數(1);
                    break;
                default:
                    System.out.println("只能打戰術牌");
            }
        }

    }
}
