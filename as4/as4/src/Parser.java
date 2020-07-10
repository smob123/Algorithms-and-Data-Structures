import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.*;
import javax.swing.JFileChooser;

/**
 * The parser and interpreter. The top level parse function, a main method for
 * testing, and several utility methods are provided. You need to implement
 * parseProgram and all the rest of the parser.
 */
public class Parser {

    /**
     * Top level parse method, called by the World
     */
    static RobotProgramNode parseFile(File code) {
        Scanner scan = null;
        try {
            scan = new Scanner(code);

            // the only time tokens can be next to each other is
            // when one of them is one of (){},;
            scan.useDelimiter("\\s+|(?=[{}(),;])|(?<=[{}(),;])");

            RobotProgramNode n = parseProgram(scan); // You need to implement this!!!

            scan.close();
            return n;
        } catch (FileNotFoundException e) {
            System.out.println("Robot program source file not found");
        } catch (ParserFailureException e) {
            System.out.println("Parser error:");
            System.out.println(e.getMessage());
            scan.close();
        }
        return null;
    }

    /**
     * For testing the parser without requiring the world
     */

    public static void main(String[] args) {
        if (args.length > 0) {
            for (String arg : args) {
                File f = new File(arg);
                if (f.exists()) {
                    System.out.println("Parsing '" + f + "'");
                    RobotProgramNode prog = parseFile(f);
                    System.out.println("Parsing completed ");
                    if (prog != null) {
                        System.out.println("================\nProgram:");
                        System.out.println(prog);
                    }
                    System.out.println("=================");
                } else {
                    System.out.println("Can't find file '" + f + "'");
                }
            }
        } else {
            while (true) {
                JFileChooser chooser = new JFileChooser(".");// System.getProperty("user.dir"));
                int res = chooser.showOpenDialog(null);
                if (res != JFileChooser.APPROVE_OPTION) {
                    break;
                }
                RobotProgramNode prog = parseFile(chooser.getSelectedFile());
                System.out.println("Parsing completed");
                if (prog != null) {
                    System.out.println("Program: \n" + prog);
                }
                System.out.println("=================");
            }
        }
        System.out.println("Done");
    }

    // Useful Patterns

    static Pattern NUMPAT = Pattern.compile("-?\\d+"); // ("-?(0|[1-9][0-9]*)");
    static Pattern OPENPAREN = Pattern.compile("\\(");
    static Pattern CLOSEPAREN = Pattern.compile("\\)");
    static Pattern OPENBRACE = Pattern.compile("\\{");
    static Pattern CLOSEBRACE = Pattern.compile("\\}");

    static Pattern ACTPAT = Pattern.compile("move|turnL|turnR|turnAround|takeFuel|wait|shieldOn|shieldOff");
    static Pattern LOOP = Pattern.compile("loop");
    static Pattern WHILE = Pattern.compile("while");
    static Pattern IF = Pattern.compile("if");
    static Pattern CONDOPPAT = Pattern.compile("and|or|not");
    static Pattern RELOPPAT = Pattern.compile("gt|lt|eq");
    static Pattern OPPAT = Pattern.compile("add|sub|mul|div");
    static Pattern SENPAT = Pattern.compile("fuelLeft|oppLR|oppFB|numBarrels|barrelLR|barrelFB|wallDist");
    static Pattern VARPAT = Pattern.compile("\\$[A-Za-z][A-Za-z0-9]*");
    static Pattern EXPPAT = Pattern.compile(NUMPAT.pattern() + "|" + SENPAT.pattern() + "|" + OPPAT.pattern() + "|" + VARPAT.pattern());

    /**
     * PROG ::= STMT+
     */
    static RobotProgramNode parseProgram(Scanner s) {
        ArrayList<RobotProgramNode> stmts = new ArrayList<>();
        while (s.hasNext()) {
            stmts.add(parseSTMT(s));
        }

        return new RobotInstructions(stmts);
    }

    static STMT parseSTMT(Scanner s) {
        RobotProgramNode node = null;
        if (s.hasNext(ACTPAT)) {
            node = parseACT(s);
        } else if (checkFor(LOOP, s)) {
            node = parseLoop(s);
        } else if (checkFor(WHILE, s)) {
            node = parseWhile(s);
        } else if (checkFor(IF, s)) {
            node = parseIf(s);
        } else if (s.hasNext(VARPAT)) {
            String varName = require(VARPAT, "invalid var name", s);
            node = assignVar(varName, s);
        } else {
            fail("invalid \"STMT\"", s);
        }

        return new STMT(node);
    }

    static RobotProgramNode parseACT(Scanner s) {
        ACT node = null;
        if (checkFor("move", s)) {
            EXP exp = new EXP(1);

            if (s.hasNext(OPENPAREN)) {
                s.next();
                exp = parseEXP(s);
                require(CLOSEPAREN, "no closing parentheses in \"ACT\"", s);
            }

            node = new ACT(ACT.ACTs.move, exp);
        } else if (checkFor("turnL", s)) {
            node = new ACT(ACT.ACTs.turnL);
        } else if (checkFor("turnR", s)) {
            node = new ACT(ACT.ACTs.turnR);
        } else if (checkFor("turnAround", s)) {
            node = new ACT(ACT.ACTs.turnAround);
        } else if (checkFor("takeFuel", s)) {
            node = new ACT(ACT.ACTs.takeFuel);
        } else if (checkFor("wait", s)) {
            EXP exp = new EXP(1);

            if (s.hasNext(OPENPAREN)) {
                s.next();
                exp = parseEXP(s);
                require(CLOSEPAREN, "no closing parentheses in \"ACT\"", s);
            }

            node = new ACT(ACT.ACTs.wait, exp);

        } else if (checkFor("shieldOn", s)) {
            node = new ACT(ACT.ACTs.shieldOn);
        } else if (checkFor("shieldOff", s)) {
            node = new ACT(ACT.ACTs.shieldOff);
        } else {
            fail("invalid act", s);
        }

        require(";", "no semicolon after statement", s);

        return node;
    }

    static RobotProgramNode parseLoop(Scanner s) {
        return new Loop(parseBlock(s));
    }

    static BLOCK parseBlock(Scanner s) {
        require(OPENBRACE, "no opening brace", s);

        ArrayList<STMT> stmts = new ArrayList<>();

        while (!s.hasNext(CLOSEBRACE)) {
            stmts.add(parseSTMT(s));
        }

        if (stmts.isEmpty()) {
            fail("empty \"BLOCK\"", s);
        }

        require(CLOSEBRACE, "no closing brace", s);

        return new BLOCK(stmts);
    }

    static RobotProgramNode parseWhile(Scanner s) {
        require(OPENPAREN, "no opening parentheses in \"WHILE\"", s);

        if (!s.hasNext(CONDOPPAT) && !s.hasNext(RELOPPAT)) {
            fail("invalid while condition", s);
        }

        Condition condition = parseCondition(s);

        require(CLOSEPAREN, "no closing parentheses in \"WHILE\"", s);

        BLOCK block = parseBlock(s);

        return new WhileLoop(condition, block);
    }

    static RobotProgramNode parseIf(Scanner s) {
        require(OPENPAREN, "no opening parentheses", s);

        if (!s.hasNext(CONDOPPAT) && !s.hasNext(RELOPPAT)) {
            fail("invalid if condition", s);
        }

        Condition condition = parseCondition(s);

        require(CLOSEPAREN, "no closing parentheses", s);

        BLOCK block = parseBlock(s);

        IfStatement ifStatement = new IfStatement(condition, block);

        while (s.hasNext("elif")) {
            s.next();
            IfStatement elseIf = parseElseIf(s);
            ifStatement.elseIfs.add(elseIf);
        }
        if (s.hasNext("else")) {
            s.next();
            ifStatement.elseStatement = parseElse(ifStatement, s);
        }

        System.out.println(ifStatement);

        return ifStatement;
    }

    static IfStatement parseElseIf(Scanner s) {
        require(OPENPAREN, "no opening parentheses", s);

        if (!s.hasNext(CONDOPPAT) && !s.hasNext(RELOPPAT)) {
            fail("invalid if condition", s);
        }

        Condition condition = parseCondition(s);

        require(CLOSEPAREN, "no closing parentheses", s);

        BLOCK block = parseBlock(s);

        return new IfStatement(condition, block);
    }

    static ElseStatement parseElse(IfStatement parent, Scanner s) {
        return new ElseStatement(parent, parseBlock(s));
    }

    static Condition parseCondition(Scanner s) {
        if (s.hasNext(CONDOPPAT)) {
            return parseOperator(s);
        } else if (s.hasNext(RELOPPAT)) {
            return parseRelop(s);
        } else {
            fail("invalid \"COND\"", s);
        }

        return null;
    }

    static Condition parseOperator(Scanner s) {
        Condition.OPERATOR operator = null;
        switch (s.next()) {
            case "and":
                operator = Condition.OPERATOR.and;
                break;
            case "or":
                operator = Condition.OPERATOR.or;
                break;
            case "not":
                operator = Condition.OPERATOR.not;
                break;
            default:
                fail("invalid \"COND\" operator", s);
                break;
        }

        require(OPENPAREN, "no opening parentheses in \"COND\" operator", s);
        Condition c1 = parseCondition(s);

        if (operator == Condition.OPERATOR.not) {
            require(CLOSEPAREN, "no closing parentheses in \"COND\" operator", s);
            return new Condition(operator, c1);
        }

        require(",", "no comma between \"COND\" operators", s);
        Condition c2 = parseCondition(s);
        require(CLOSEPAREN, "no closing parentheses in \"COND\" operator", s);

        return new Condition(c1, c2, operator);
    }

    static Condition parseRelop(Scanner s) {
        RELOP relop = null;
        switch (s.next()) {
            case "gt":
                relop = new RELOP(RELOP.RELOPs.gt);
                break;
            case "lt":
                relop = new RELOP(RELOP.RELOPs.lt);
                break;
            case "eq":
                relop = new RELOP(RELOP.RELOPs.eq);
                break;
            default:
                fail("invalid \"RELOP\"", s);
                break;
        }

        require(OPENPAREN, "no opening parentheses in \"COND\"", s);

        SEN sen = null;
        SEN sen2 = null;
        EXP exp = null;
        EXP exp2 = null;

        if (s.hasNext(SENPAT)) {
            sen = parseSEN(s);
        } else if (s.hasNext(EXPPAT)) {
            exp = parseEXP(s);
        } else {
            fail("invalid first \"RELOP\" condition", s);
        }

        require(",", "no comma between \"RELOP\"", s);

        if (s.hasNext(SENPAT)) {
            sen2 = parseSEN(s);
        } else if (s.hasNext(EXPPAT)) {
            exp2 = parseEXP(s);
        } else {
            fail("invalid second \"RELOP\" condition", s);
        }

        require(CLOSEPAREN, "no closing parentheses in \"COND\"", s);

        Condition c = null;

        if (sen != null && sen2 != null) {
            c = new Condition(relop, sen, sen2);
        } else if (sen != null && exp2 != null) {
            c = new Condition(relop, sen, exp2);
        } else if (exp != null && sen2 != null) {
            c = new Condition(relop, sen2, exp);
        } else if (exp != null && exp2 != null) {
            c = new Condition(relop, exp, exp2);
        }

        return c;
    }

    static SEN parseSEN(Scanner s) {
        switch (s.next()) {
            case "fuelLeft":
                return new SEN(SEN.SENs.FUEL_LEFT);
            case "oppLR":
                return new SEN(SEN.SENs.OPP_LR);
            case "oppFB":
                return new SEN(SEN.SENs.OPP_FB);
            case "numBarrels":
                return new SEN(SEN.SENs.NUM_BARRELS);
            case "barrelLR":
                if (s.hasNext(OPENPAREN)) {
                    s.next();
                    EXP exp = parseEXP(s);
                    require(CLOSEPAREN, "no closing parentheses after \"n\" in \"SEN\"", s);
                    return new SEN(SEN.SENs.N_BARREL_LR, exp);
                }
                return new SEN(SEN.SENs.BARREL_LR);
            case "barrelFB":
                if (s.hasNext(OPENPAREN)) {
                    s.next();
                    EXP exp = parseEXP(s);
                    require(CLOSEPAREN, "no closing parentheses after \"n\" in \"SEN\"", s);
                    return new SEN(SEN.SENs.N_BARREL_FB, exp);
                }
                return new SEN(SEN.SENs.BARREL_FB);
            case "wallDist":
                return new SEN(SEN.SENs.WALL_DIST);
        }

        fail("invalid SEN", s);
        return null;
    }

    static EXP parseEXP(Scanner s) {
        if (s.hasNext(NUMPAT)) {
            int n = requireInt(NUMPAT, "invalid number", s);
            return new EXP(n);
        } else if (s.hasNext(SENPAT)) {
            return new EXP(parseSEN(s));
        } else if (s.hasNext(OPPAT)) {
            return new EXP(parseOP(s));
        } else if (s.hasNext(VARPAT)) {
            String varName = require(VARPAT, "invalid var name", s);
            return new EXP(varName);
        }

        fail("invalid \"EXP\"", s);
        return null;
    }

    static OP parseOP(Scanner s) {
        OP.OPs ops = null;
        switch (s.next()) {
            case "add":
                ops = OP.OPs.add;
                break;
            case "sub":
                ops = OP.OPs.sub;
                break;
            case "mul":
                ops = OP.OPs.mul;
                break;
            case "div":
                ops = OP.OPs.div;
                break;
            default:
                fail("invalid \"OP\"", s);
                break;
        }

        require(OPENPAREN, "no opening parentheses in \"OP\"", s);

        EXP exp1 = parseEXP(s);
        require(",", "no comma between \"OP\" params", s);

        EXP exp2 = parseEXP(s);
        require(CLOSEPAREN, "no closing parentheses in \"OP\"", s);

        return new OP(exp1, exp2, ops);
    }

    static AssignVar assignVar(String varName, Scanner s) {
        require("=", "no equals sign, and var hasn't been assigned before", s);
        AssignVar var = null;

        if (s.hasNext(EXPPAT)) {
            EXP val = parseEXP(s);
            var = new AssignVar(varName, val);

        } else if (s.hasNext(SENPAT)) {
            SEN val = parseSEN(s);
            var = new AssignVar(varName, val);
        } else {
            fail("invalid var assignment", s);
        }

        require(";", "no semicolon after var assignment", s);

        return var;
    }

    // utility methods for the parser

    /**
     * Report a failure in the parser.
     */
    static void fail(String message, Scanner s) {
        String msg = message + "\n   @ ...";
        for (int i = 0; i < 5 && s.hasNext(); i++) {
            msg += " " + s.next();
        }
        throw new ParserFailureException(msg + "...");
    }

    /**
     * Requires that the next token matches a pattern if it matches, it consumes and
     * returns the token, if not, it throws an exception with an error message
     */
    static String require(String p, String message, Scanner s) {
        if (s.hasNext(p)) {
            return s.next();
        }
        fail(message, s);
        return null;
    }

    static String require(Pattern p, String message, Scanner s) {
        if (s.hasNext(p)) {
            return s.next();
        }
        fail(message, s);
        return null;
    }

    /**
     * Requires that the next token matches a pattern (which should only match a
     * number) if it matches, it consumes and returns the token as an integer if
     * not, it throws an exception with an error message
     */
    static int requireInt(String p, String message, Scanner s) {
        if (s.hasNext(p) && s.hasNextInt()) {
            return s.nextInt();
        }
        fail(message, s);
        return -1;
    }

    static int requireInt(Pattern p, String message, Scanner s) {
        if (s.hasNext(p) && s.hasNextInt()) {
            return s.nextInt();
        }
        fail(message, s);
        return -1;
    }

    /**
     * Checks whether the next token in the scanner matches the specified pattern,
     * if so, consumes the token and return true. Otherwise returns false without
     * consuming anything.
     */
    static boolean checkFor(String p, Scanner s) {
        if (s.hasNext(p)) {
            s.next();
            return true;
        } else {
            return false;
        }
    }

    static boolean checkFor(Pattern p, Scanner s) {
        if (s.hasNext(p)) {
            s.next();
            return true;
        } else {
            return false;
        }
    }

}

// You could add the node classes here, as long as they are not declared public (or private)

class ACT implements RobotProgramNode {
    public enum ACTs {
        move,
        turnL,
        turnR,
        turnAround,
        shieldOn,
        shieldOff,
        takeFuel,
        wait
    }

    ACTs act;
    EXP exp;
    int nRuns = 1;

    public ACT(ACTs act) {
        this.act = act;
    }

    public ACT(ACTs act, EXP exp) {
        this.act = act;
        this.exp = exp;
    }

    @Override
    public void execute(Robot robot) {
        switch (act) {
            case move:
                if (exp != null) {
                    nRuns = exp.getEXP(robot);
                }
                for (int i = 0; i < nRuns; i++) {
                    robot.move();
                }
                break;
            case turnL:
                robot.turnLeft();
                break;
            case turnR:
                robot.turnRight();
                break;
            case turnAround:
                robot.turnAround();
                break;
            case wait:
                if (exp != null) {
                    nRuns = exp.getEXP(robot);
                }
                for (int i = 0; i < nRuns; i++) {
                    robot.idleWait();
                }
                break;
            case shieldOn:
                robot.setShield(true);
                break;
            case shieldOff:
                robot.setShield(false);
                break;
            case takeFuel:
                robot.takeFuel();
                break;
        }
    }

    @Override
    public String toString() {
        String s = act.toString();

        if (nRuns > 1) {
            s += "(" + nRuns + ")";
        }

        return s;
    }
}


class STMT implements RobotProgramNode {
    RobotProgramNode child;

    public STMT(RobotProgramNode child) {
        this.child = child;
    }

    @Override
    public void execute(Robot robot) {
        child.execute(robot);
    }

    @Override
    public String toString() {
        return child.toString();
    }
}


class SEN {
    public enum SENs {
        FUEL_LEFT,
        OPP_LR,
        OPP_FB,
        NUM_BARRELS,
        BARREL_LR,
        N_BARREL_LR,
        BARREL_FB,
        N_BARREL_FB,
        WALL_DIST
    }

    EXP n;

    SENs sen;

    public SEN(SENs sen) {
        this.sen = sen;
    }

    public SEN(SENs sen, EXP n) {
        this.sen = sen;
        this.n = n;
    }

    public int getSEN(Robot robot) {
        switch (sen) {
            case FUEL_LEFT:
                return robot.getFuel();
            case OPP_LR:
                return robot.getOpponentLR();
            case OPP_FB:
                return robot.getOpponentFB();
            case NUM_BARRELS:
                return robot.numBarrels();
            case BARREL_LR:
                return robot.getClosestBarrelLR();
            case N_BARREL_LR:
                return robot.getBarrelLR(n.getEXP(robot));
            case BARREL_FB:
                return robot.getClosestBarrelFB();
            case N_BARREL_FB:
                return robot.getBarrelFB(n.getEXP(robot));
            case WALL_DIST:
                return robot.getDistanceToWall();
        }

        return 0;
    }

    @Override
    public String toString() {
        switch (sen) {
            case FUEL_LEFT:
                return SENs.FUEL_LEFT.name();
            case OPP_LR:
                return SENs.OPP_LR.name();
            case OPP_FB:
                return SENs.OPP_FB.name();
            case NUM_BARRELS:
                return SENs.NUM_BARRELS.name();
            case BARREL_LR:
                return SENs.BARREL_LR.name();
            case N_BARREL_LR:
                return SENs.N_BARREL_LR.name();
            case BARREL_FB:
                return SENs.BARREL_FB.name();
            case N_BARREL_FB:
                return SENs.N_BARREL_FB.name();
            case WALL_DIST:
                return SENs.WALL_DIST.name();
        }

        return "\n";
    }
}


class EXP {
    SEN sen;
    int num = (int) Double.POSITIVE_INFINITY;
    OP op;
    String varName;
    AssignVar var;

    public EXP(String varName) {
        this.varName = varName;
    }

    public EXP(AssignVar var) {
        this.var = var;
    }

    public EXP(SEN sen) {
        this.sen = sen;
    }

    public EXP(int num) {
        this.num = num;
    }

    public EXP(OP op) {
        this.op = op;
    }

    public int getEXP(Robot robot) {
        if (sen != null) {
            return sen.getSEN(robot);
        } else if (var != null) {
            var.execute(robot);
            return robot.getVariable(var.name);
        } else if (varName != null) {
            return robot.getVariable(varName);
        } else if (op != null) {
            System.out.println(op);
            System.out.println(op.result(robot));
            return op.result(robot);
        } else if (num < (int) Double.POSITIVE_INFINITY) {
            return num;
        }

        return 0;
    }

    @Override
    public String toString() {
        String s = "";

        if (sen != null) {
            s = sen.toString();
        } else if (op != null) {
            s = op.toString();
        } else if (var != null) {
            s += var;
        } else if (varName != null) {
            return varName;
        } else if (num < Integer.MAX_VALUE) {
            s = Integer.toString(num);
        }

        return s;
    }
}


class OP {
    EXP exp1;
    EXP exp2;
    OPs operation;

    public enum OPs {
        add,
        sub,
        mul,
        div
    }

    public OP(EXP exp1, EXP exp2, OPs operation) {
        this.exp1 = exp1;
        this.exp2 = exp2;
        this.operation = operation;
    }

    public int result(Robot robot) {
        switch (operation) {
            case add:
                return exp1.getEXP(robot) + exp2.getEXP(robot);
            case sub:
                return exp1.getEXP(robot) - exp2.getEXP(robot);
            case mul:
                return exp1.getEXP(robot) * exp2.getEXP(robot);
            case div:
                return exp1.getEXP(robot) / exp2.getEXP(robot);
        }

        return 0;
    }

    @Override
    public String toString() {
        return operation + "(" + exp1 + ", " + exp2 + ")";
    }
}


class RELOP {
    public enum RELOPs {
        gt,
        lt,
        eq
    }

    RELOPs relop;

    public RELOP(RELOPs relop) {
        this.relop = relop;
    }

    public RELOPs getRelop() {
        return relop;
    }
}


class Condition {
    public enum OPERATOR {
        and,
        or,
        not,
        is
    }

    Condition firstCondition;
    Condition secondCondition;
    OPERATOR operator;
    RELOP relop;
    SEN sen;
    SEN sen2;
    EXP exp;
    EXP exp2;

    public Condition(Condition firstCondition, Condition secondCondition, OPERATOR operator) {
        this.firstCondition = firstCondition;
        this.secondCondition = secondCondition;
        this.operator = operator;
    }

    public Condition(OPERATOR operator, Condition condition) {
        this.operator = operator;
        this.firstCondition = condition;
    }

    public Condition(RELOP relop, SEN sen, SEN sen2) {
        this.relop = relop;
        this.sen = sen;
        this.sen2 = sen2;
    }

    Condition(RELOP relop, EXP exp, EXP exp2) {
        this.relop = relop;
        this.exp = exp;
        this.exp2 = exp2;
    }

    public Condition(RELOP relop, SEN sen, EXP exp) {
        this.relop = relop;
        this.sen = sen;
        this.exp = exp;
    }

    public boolean isTrue(Robot robot) {
        if (firstCondition != null && secondCondition != null) {
            switch (operator) {
                case and:
                    return firstCondition.isTrue(robot) && secondCondition.isTrue(robot);
                case or:
                    return firstCondition.isTrue(robot) || secondCondition.isTrue(robot);
            }
        } else if (relop != null && sen != null && exp != null) {
            switch (relop.getRelop()) {
                case eq:
                    return sen.getSEN(robot) == exp.getEXP(robot);
                case gt:
                    return sen.getSEN(robot) > exp.getEXP(robot);
                case lt:
                    return sen.getSEN(robot) < exp.getEXP(robot);
            }
        } else if (relop != null && sen != null && sen2 != null) {
            switch (relop.getRelop()) {
                case eq:
                    return sen.getSEN(robot) == sen2.getSEN(robot);
                case gt:
                    return sen.getSEN(robot) > sen2.getSEN(robot);
                case lt:
                    return sen.getSEN(robot) < sen2.getSEN(robot);
            }
        } else if (relop != null && exp != null && exp2 != null) {
            switch (relop.getRelop()) {
                case eq:
                    return exp.getEXP(robot) == exp2.getEXP(robot);
                case gt:
                    return exp.getEXP(robot) > exp2.getEXP(robot);
                case lt:
                    return exp.getEXP(robot) < exp2.getEXP(robot);
            }
        } else if (firstCondition != null) {
            switch (operator) {
                case is:
                    return firstCondition.isTrue(robot);
                case not:
                    return !firstCondition.isTrue(robot);
            }
        }

        return false;
    }

    @Override
    public String toString() {
        String s = "(";

        if (firstCondition != null && secondCondition != null) {
            switch (operator) {
                case and:
                    s += "and(" + firstCondition + ", " + secondCondition + ")";
                    break;
                case or:
                    s += "or(" + firstCondition + ", " + secondCondition + ")";
                    break;
            }
        } else if (relop != null && sen != null && exp != null) {
            switch (relop.getRelop()) {
                case eq:
                    s += "eq(" + sen + ", " + exp + ")";
                    break;
                case gt:
                    s += "gt(" + sen + ", " + exp + ")";
                    break;
                case lt:
                    s += "lt(" + sen + ", " + exp + ")";
                    break;
            }
        } else if (relop != null && sen != null && sen2 != null) {
            switch (relop.getRelop()) {
                case eq:
                    s += "eq(" + sen + ", " + sen2 + ")";
                    break;
                case gt:
                    s += "gt(" + sen + ", " + sen2 + ")";
                    break;
                case lt:
                    s += "lt(" + sen + ", " + sen2 + ")";
                    break;
            }
        } else if (relop != null && exp != null && exp2 != null) {
            switch (relop.getRelop()) {
                case eq:
                    s += "eq(" + exp + ", " + exp2 + ")";
                    break;
                case gt:
                    s += "gt(" + exp + ", " + exp2 + ")";
                    break;
                case lt:
                    s += "lt(" + exp + ", " + exp2 + ")";
                    break;
            }
        } else if (firstCondition != null) {
            switch (operator) {
                case is:
                    s += firstCondition;
                    break;
                case not:
                    s += "not" + firstCondition;
                    break;
            }
        }

        return s + ")";
    }
}


class BLOCK implements RobotProgramNode {
    ArrayList<STMT> stmts;

    public BLOCK(ArrayList<STMT> stmts) {
        this.stmts = stmts;
    }

    @Override
    public void execute(Robot robot) {
        for (STMT stmt : stmts) {
            if (robot.isDead()) {
                break;
            }

            stmt.execute(robot);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("{\n");

        for (STMT stmt : stmts) {
            s.append(stmt.toString()).append("\n");
        }

        return s.toString() + "}";
    }
}


class Loop implements RobotProgramNode {
    BLOCK block;

    public Loop(BLOCK block) {
        this.block = block;
    }

    @Override
    public void execute(Robot robot) {
        while (!robot.isDead()) {
            block.execute(robot);
        }
    }

    @Override
    public String toString() {
        return "loop " + block.toString();
    }
}


class WhileLoop implements RobotProgramNode {
    private Condition condition;
    BLOCK block;

    WhileLoop(Condition condition, BLOCK block) {
        this.condition = condition;
        this.block = block;
    }

    @Override
    public void execute(Robot robot) {
        while (!robot.isDead() && condition.isTrue(robot)) {
            block.execute(robot);
        }
    }

    @Override
    public String toString() {
        return "While" + condition.toString() + block.toString() + "\n";
    }
}



class IfStatement implements RobotProgramNode {
    public Condition condition;
    BLOCK block;
    public ElseStatement elseStatement;
    public ArrayList<IfStatement> elseIfs = new ArrayList<>();

    IfStatement(Condition condition, BLOCK block) {
        this.condition = condition;
        this.block = block;
    }

    @Override
    public void execute(Robot robot) {
        if (robot.isDead()) {
            return;
        }

        boolean execElse = true;

        if (condition.isTrue(robot)) {
            block.execute(robot);
        } else if (!elseIfs.isEmpty()) {
            for (IfStatement statement : elseIfs) {
                if (statement.condition.isTrue(robot)) {
                    statement.execute(robot);
                    execElse = false;
                    break;
                }
            }
        }

        if (execElse && elseStatement != null) {
            elseStatement.execute(robot);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("If " + condition.toString() + block.toString());

        for (IfStatement statement : elseIfs) {
            s.append("elif").append(statement.condition).append(block.toString()).append("\n");
        }

        if (elseStatement != null) {
            s.append(elseStatement.toString());
        }

        return s.toString();
    }
}

class ElseStatement implements RobotProgramNode {
    IfStatement parent;
    BLOCK block;

    ElseStatement(IfStatement parent, BLOCK block) {
        this.parent = parent;
        this.block = block;
    }

    @Override
    public void execute(Robot robot) {
        if (!robot.isDead() && !parent.condition.isTrue(robot)) {
            block.execute(robot);
        }
    }

    @Override
    public String toString() {
        return "Else" + block.toString();
    }
}

class AssignVar implements RobotProgramNode {
    String name;
    EXP exp;
    SEN sen;

    public AssignVar(String name, EXP exp) {
        this.name = name;
        this.exp = exp;
    }

    public AssignVar(String name, SEN sen) {
        this.name = name;
        this.sen = sen;
    }

    @Override
    public void execute(Robot robot) {
        int n = 0;
        if (exp != null) {
            n = exp.getEXP(robot);
        } else if (sen != null) {
            sen.getSEN(robot);
        }
        robot.addVariable(name, n);
    }

    @Override
    public String toString() {
        String s = "" + name + " = ";

        if (exp != null) {
            s += exp;
        } else if (sen != null) {
            s += sen;
        }

        return s;
    }
}

class RobotInstructions implements RobotProgramNode {
    ArrayList<RobotProgramNode> instructions;

    public RobotInstructions(ArrayList<RobotProgramNode> instructions) {
        this.instructions = instructions;
    }

    @Override
    public void execute(Robot robot) {
        for (RobotProgramNode rpn : instructions) {
            rpn.execute(robot);
        }
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();

        for (RobotProgramNode rpn : instructions) {
            s.append(rpn.toString());
        }

        return s.toString();
    }
}
