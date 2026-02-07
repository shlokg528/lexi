package src;
import java.util.*;
import java.io.*;
import java.util.regex.*;

/**
 * Lexi Programming Language Interpreter
 * Version 1.0
 * 
 * A natural language programming language that feels like writing English.
 * 
 * @author Your Name
 * @version 1.0
 */
public class Lexi {
    
    // ============================================================
    // VERSION AND BRANDING
    // ============================================================
    
    private static final String VERSION = "1.0";
    private static final String BANNER = 
        "╔════════════════════════════════════════════╗\n" +
        "║             Lexi " + VERSION + "                      ║\n" +
        "║  Natural Language Programming Language     ║\n" +
        "╚════════════════════════════════════════════╝";
    
    // ============================================================
    // GLOBAL STATE
    // ============================================================
    
    private static Map<String, Object> globalVariables = new HashMap<>();
    private static Map<String, Function> functions = new HashMap<>();
    private static List<String> program = new ArrayList<>();
    private static int currentLine = 0;
    private static Scanner userInput = new Scanner(System.in);
    private static boolean debugMode = false;
    
    // For handling return values
    private static Object returnValue = null;
    private static boolean hasReturned = false;
    
    // ============================================================
    // FUNCTION DEFINITION
    // ============================================================
    
    static class Function {
        String name;
        List<String> parameters;
        List<String> body;
        
        Function(String name, List<String> parameters, List<String> body) {
            this.name = name;
            this.parameters = parameters;
            this.body = body;
        }
    }
    
    // ============================================================
    // EXECUTION CONTEXT (Local Scopes)
    // ============================================================
    
    static class ExecutionContext {
        Map<String, Object> localVariables = new HashMap<>();
        ExecutionContext parent;
        String scopeName;
        
        ExecutionContext(ExecutionContext parent, String scopeName) {
            this.parent = parent;
            this.scopeName = scopeName;
        }
        
        Object getVariable(String name) {
            if (localVariables.containsKey(name)) {
                return localVariables.get(name);
            }
            if (parent != null) {
                return parent.getVariable(name);
            }
            return globalVariables.get(name);
        }
        
        void setVariable(String name, Object value) {
            localVariables.put(name, value);
        }
        
        boolean hasVariable(String name) {
            return localVariables.containsKey(name) || 
                   (parent != null && parent.hasVariable(name)) ||
                   globalVariables.containsKey(name);
        }
    }
    
    // ============================================================
    // CUSTOM EXCEPTION
    // ============================================================
    
    static class LexiException extends Exception {
        LexiException(String message) {
            super("Line " + (currentLine + 1) + ": " + message);
        }
    }
    
    // ============================================================
    // MAIN ENTRY POINT
    // ============================================================
    
    public static void main(String[] args) {
        System.out.println(BANNER);
        System.out.println();
        
        if (args.length > 0) {
            // File execution mode
            loadFile(args[0]);
            try {
                runProgram();
            } catch (LexiException e) {
                displayError(e);
            }
        } else {
            // Interactive REPL mode
            startREPL();
        }
    }
    
    // ============================================================
    // REPL (Read-Eval-Print Loop)
    // ============================================================
    
    private static void startREPL() {
        System.out.println("Welcome to Lexi! Type HELP for commands.");
        System.out.println();
        
        Scanner scanner = new Scanner(System.in);
        
        while (true) {
            System.out.print("lexi> ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty()) continue;
            
            // Handle REPL commands
            if (input.equalsIgnoreCase("exit") || input.equalsIgnoreCase("quit")) {
                System.out.println("\nGoodbye! Thanks for using Lexi.");
                break;
            }
            
            if (input.equalsIgnoreCase("help")) {
                showHelp();
                continue;
            }
            
            if (input.equalsIgnoreCase("clear")) {
                program.clear();
                globalVariables.clear();
                functions.clear();
                System.out.println("Memory cleared.");
                continue;
            }
            
            if (input.equalsIgnoreCase("debug")) {
                debugMode = !debugMode;
                System.out.println("Debug mode: " + (debugMode ? "ON" : "OFF"));
                continue;
            }
            
            if (input.equalsIgnoreCase("run")) {
                try {
                    runProgram();
                } catch (LexiException e) {
                    displayError(e);
                }
                System.out.println();
                continue;
            }
            
            if (input.equalsIgnoreCase("show")) {
                showProgramState();
                continue;
            }
            
            // Add line to program
            program.add(input);
        }
    }
    
    // ============================================================
    // HELP SYSTEM
    // ============================================================
    
    private static void showHelp() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║           LEXI HELP                        ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
        
        System.out.println("REPL COMMANDS:");
        System.out.println("  RUN      - Execute your program");
        System.out.println("  HELP     - Show this help");
        System.out.println("  CLEAR    - Clear all memory");
        System.out.println("  DEBUG    - Toggle debug mode");
        System.out.println("  SHOW     - Show program state");
        System.out.println("  EXIT     - Quit Lexi");
        System.out.println();
        
        System.out.println("LANGUAGE BASICS:");
        System.out.println("  Variables:");
        System.out.println("    create a number called x with value 10");
        System.out.println("    set score to 90");
        System.out.println("    increase counter by 1");
        System.out.println();
        
        System.out.println("  Output:");
        System.out.println("    display \"Hello World\"");
        System.out.println("    show score");
        System.out.println("    print \"Score: \" + score");
        System.out.println();
        
        System.out.println("  Input:");
        System.out.println("    ask for name");
        System.out.println("    get input for age");
        System.out.println();
        
        System.out.println("  Functions with return:");
        System.out.println("    function square n");
        System.out.println("      return n * n");
        System.out.println("    end");
        System.out.println("    set result to call square with 5");
        System.out.println();
        
        System.out.println("  Conditionals:");
        System.out.println("    if score >= 90");
        System.out.println("      display \"A\"");
        System.out.println("    elseif score >= 80");
        System.out.println("      display \"B\"");
        System.out.println("    else");
        System.out.println("      display \"C\"");
        System.out.println("    end");
        System.out.println();
        
        System.out.println("  Loops:");
        System.out.println("    for i from 1 to 10");
        System.out.println("      display i");
        System.out.println("    end");
        System.out.println();
        
        System.out.println("  Expressions:");
        System.out.println("    (10 + 5) * 2    # Parentheses");
        System.out.println("    2 ^ 8           # Power");
        System.out.println("    x * y + z       # Precedence");
        System.out.println();
        
        System.out.println("Visit github.com/yourname/lexi for more examples!");
        System.out.println();
    }
    
    // ============================================================
    // PROGRAM STATE DISPLAY
    // ============================================================
    
    private static void showProgramState() {
        System.out.println("\n╔════════════════════════════════════════════╗");
        System.out.println("║         PROGRAM STATE                      ║");
        System.out.println("╚════════════════════════════════════════════╝\n");
        
        System.out.println("VARIABLES:");
        if (globalVariables.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (Map.Entry<String, Object> entry : globalVariables.entrySet()) {
                System.out.println("  " + entry.getKey() + " = " + formatValue(entry.getValue()));
            }
        }
        System.out.println();
        
        System.out.println("FUNCTIONS:");
        if (functions.isEmpty()) {
            System.out.println("  (none)");
        } else {
            for (Map.Entry<String, Function> entry : functions.entrySet()) {
                Function f = entry.getValue();
                System.out.print("  " + f.name + "(");
                System.out.print(String.join(", ", f.parameters));
                System.out.println(")");
            }
        }
        System.out.println();
        
        System.out.println("PROGRAM LINES: " + program.size());
        System.out.println();
    }
    
    // ============================================================
    // FILE LOADING
    // ============================================================
    
    private static void loadFile(String path) {
        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                program.add(line);
            }
            System.out.println("Loaded: " + path);
            System.out.println();
        } catch (IOException e) {
            System.err.println("Error loading file: " + e.getMessage());
            System.exit(1);
        }
    }
    
    // ============================================================
    // PROGRAM EXECUTION (Structured Block Execution)
    // ============================================================
    
    private static void runProgram() throws LexiException {
        currentLine = 0;
        hasReturned = false;
        returnValue = null;
        ExecutionContext globalContext = new ExecutionContext(null, "global");
        executeBlock(program, 0, program.size(), globalContext);
    }
    
    /**
     * Execute a block of code using structured execution
     * This is cleaner than jumping with instruction pointers
     */
    private static void executeBlock(List<String> code, int startLine, int endLine, 
                                     ExecutionContext context) throws LexiException {
        int i = startLine;
        
        while (i < endLine && !hasReturned) {
            currentLine = i;
            String line = code.get(i).trim();
            
            if (debugMode) {
                System.out.println("[DEBUG] Line " + (i + 1) + ": " + line);
            }
            
            // Skip empty lines and comments
            if (line.isEmpty() || line.startsWith("#") || line.startsWith("//")) {
                i++;
                continue;
            }
            
            // Handle different statement types
            if (isReturnStatement(line)) {
                handleReturn(line, context);
                return;
            } else if (isIfStatement(line)) {
                i = handleIfBlock(code, i, endLine, context);
            } else if (isWhileStatement(line)) {
                i = handleWhileBlock(code, i, endLine, context);
            } else if (isForStatement(line)) {
                i = handleForBlock(code, i, endLine, context);
            } else if (isFunctionDefinition(line)) {
                i = handleFunctionDefinition(code, i, endLine);
            } else if (isBlockEnd(line)) {
                return; // Exit this block
            } else {
                executeStatement(line, context);
                i++;
            }
        }
    }
    
    // ============================================================
    // STATEMENT EXECUTION
    // ============================================================
    
    private static void executeStatement(String line, ExecutionContext context) 
            throws LexiException {
        
        // Try all natural language patterns
        if (tryCreateVariable(line, context)) return;
        if (trySetVariable(line, context)) return;
        if (tryIncreaseVariable(line, context)) return;
        if (tryDecreaseVariable(line, context)) return;
        if (tryDisplay(line, context)) return;
        if (tryInput(line, context)) return;
        if (tryFunctionCall(line, context)) return;
        if (tryCreateArray(line, context)) return;
        if (tryArrayAdd(line, context)) return;
        
        throw new LexiException("Unknown command: " + line);
    }
    
    // ============================================================
    // NATURAL LANGUAGE PATTERNS
    // ============================================================
    
    /**
     * Pattern: "create a number called score with value 90"
     */
    private static boolean tryCreateVariable(String line, ExecutionContext context) 
            throws LexiException {
        
        Pattern p = Pattern.compile(
            "create\\s+a\\s+(?:number|variable|string)\\s+called\\s+(\\w+)\\s+with\\s+value\\s+(.+)",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher m = p.matcher(line);
        if (m.matches()) {
            String varName = m.group(1);
            String valueExpr = m.group(2);
            
            Object value = evaluateExpression(valueExpr, context);
            context.setVariable(varName, value);
            return true;
        }
        
        return false;
    }
    
    /**
     * Pattern: "set score to 100"
     * Pattern: "set result to call square with 5"
     */
    private static boolean trySetVariable(String line, ExecutionContext context) 
            throws LexiException {
        
        Pattern p1 = Pattern.compile(
            "(set|make|let)\\s+(\\w+)\\s+(?:to|equal to|=|be)\\s+(.+)",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher m = p1.matcher(line);
        if (m.matches()) {
            String varName = m.group(2);
            String valueExpr = m.group(3);
            
            // Check if it's a function call with return value
            if (valueExpr.toLowerCase().contains("call ")) {
                Object value = evaluateFunctionCallExpression(valueExpr, context);
                context.setVariable(varName, value);
            } else {
                Object value = evaluateExpression(valueExpr, context);
                context.setVariable(varName, value);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Pattern: "increase score by 5"
     */
    private static boolean tryIncreaseVariable(String line, ExecutionContext context) 
            throws LexiException {
        
        Pattern p = Pattern.compile(
            "(increase|increment)\\s+(\\w+)(?:\\s+by\\s+(\\S+))?",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher m = p.matcher(line);
        if (m.matches()) {
            String varName = m.group(2);
            String amountStr = m.group(3);
            
            if (!context.hasVariable(varName)) {
                throw new LexiException("Variable '" + varName + "' not defined");
            }
            
            int amount = 1;
            if (amountStr != null) {
                amount = (int) evaluateExpression(amountStr, context);
            }
            
            int currentValue = toInt(context.getVariable(varName));
            context.setVariable(varName, currentValue + amount);
            return true;
        }
        
        return false;
    }
    
    /**
     * Pattern: "decrease score by 3"
     */
    private static boolean tryDecreaseVariable(String line, ExecutionContext context) 
            throws LexiException {
        
        Pattern p = Pattern.compile(
            "(decrease|decrement)\\s+(\\w+)(?:\\s+by\\s+(\\S+))?",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher m = p.matcher(line);
        if (m.matches()) {
            String varName = m.group(2);
            String amountStr = m.group(3);
            
            if (!context.hasVariable(varName)) {
                throw new LexiException("Variable '" + varName + "' not defined");
            }
            
            int amount = 1;
            if (amountStr != null) {
                amount = (int) evaluateExpression(amountStr, context);
            }
            
            int currentValue = toInt(context.getVariable(varName));
            context.setVariable(varName, currentValue - amount);
            return true;
        }
        
        return false;
    }
    
    /**
     * Pattern: "display score" or "display \"Hello\" + name"
     */
    private static boolean tryDisplay(String line, ExecutionContext context) 
            throws LexiException {
        
        Pattern p = Pattern.compile(
            "(display|show|print|say)\\s+(.+)",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher m = p.matcher(line);
        if (m.matches()) {
            String expr = m.group(2);
            Object value = evaluateExpression(expr, context);
            System.out.println(formatValue(value));
            return true;
        }
        
        return false;
    }
    
    /**
     * Pattern: "ask for name"
     */
    private static boolean tryInput(String line, ExecutionContext context) 
            throws LexiException {
        
        Pattern p = Pattern.compile(
            "(ask|get|input)\\s+(?:for|input for)\\s+(\\w+)",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher m = p.matcher(line);
        if (m.matches()) {
            String varName = m.group(2);
            System.out.print("? ");
            String input = userInput.nextLine();
            
            try {
                context.setVariable(varName, Integer.parseInt(input));
            } catch (NumberFormatException e) {
                context.setVariable(varName, input);
            }
            return true;
        }
        
        return false;
    }
    
    /**
     * Pattern: "call greet with \"Alice\""
     */
    private static boolean tryFunctionCall(String line, ExecutionContext context) 
            throws LexiException {
        
        Pattern p = Pattern.compile(
            "(call|run function)\\s+(\\w+)(?:\\s+with\\s+(.+))?",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher m = p.matcher(line);
        if (m.matches()) {
            String funcName = m.group(2);
            String argsStr = m.group(3);
            
            callFunction(funcName, argsStr, context);
            return true;
        }
        
        return false;
    }
    
    /**
     * Pattern: "create list names"
     */
    private static boolean tryCreateArray(String line, ExecutionContext context) {
        Pattern p = Pattern.compile(
            "create\\s+(list|array)\\s+(\\w+)",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher m = p.matcher(line);
        if (m.matches()) {
            String listName = m.group(2);
            context.setVariable(listName, new ArrayList<Object>());
            return true;
        }
        
        return false;
    }
    
    /**
     * Pattern: "add 5 to scores"
     */
    private static boolean tryArrayAdd(String line, ExecutionContext context) 
            throws LexiException {
        
        Pattern p = Pattern.compile(
            "add\\s+(.+?)\\s+to\\s+(\\w+)",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher m = p.matcher(line);
        if (m.matches()) {
            String valueStr = m.group(1).trim();
            String listName = m.group(2).trim();
            
            if (!context.hasVariable(listName)) {
                throw new LexiException("Variable '" + listName + "' not defined");
            }
            
            Object list = context.getVariable(listName);
            if (!(list instanceof ArrayList)) {
                throw new LexiException("Variable '" + listName + "' is not a list");
            }
            
            @SuppressWarnings("unchecked")
            ArrayList<Object> arr = (ArrayList<Object>) list;
            Object value = evaluateExpression(valueStr, context);
            arr.add(value);
            
            return true;
        }
        
        return false;
    }
    
    // ============================================================
    // RETURN STATEMENT
    // ============================================================
    
    private static boolean isReturnStatement(String line) {
        return line.trim().toLowerCase().startsWith("return ");
    }
    
    private static void handleReturn(String line, ExecutionContext context) 
            throws LexiException {
        String expr = line.substring(6).trim(); // Skip "return"
        returnValue = evaluateExpression(expr, context);
        hasReturned = true;
    }
    
    // ============================================================
    // FUNCTION HANDLING
    // ============================================================
    
    private static boolean isFunctionDefinition(String line) {
        String lower = line.trim().toLowerCase();
        return lower.startsWith("function ") || lower.startsWith("define function ");
    }
    
    private static int handleFunctionDefinition(List<String> code, int startLine, int maxLine) 
            throws LexiException {
        
        String line = code.get(startLine);
        
        Pattern p = Pattern.compile(
            "(?:define\\s+)?function\\s+(\\w+)(?:\\s+(.+))?",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher m = p.matcher(line);
        if (!m.find()) {
            throw new LexiException("Invalid function syntax");
        }
        
        String funcName = m.group(1);
        String paramsStr = m.group(2);
        
        List<String> parameters = new ArrayList<>();
        if (paramsStr != null && !paramsStr.trim().isEmpty()) {
            String[] params = paramsStr.trim().split("\\s+");
            for (String param : params) {
                parameters.add(param.trim());
            }
        }
        
        int blockEnd = findBlockEnd(code, startLine, maxLine);
        
        List<String> body = new ArrayList<>();
        for (int i = startLine + 1; i < blockEnd; i++) {
            body.add(code.get(i));
        }
        
        functions.put(funcName, new Function(funcName, parameters, body));
        
        return blockEnd + 1;
    }
    
    /**
     * Call function and handle return value
     */
    private static void callFunction(String funcName, String argsStr, ExecutionContext context) 
            throws LexiException {
        
        if (!functions.containsKey(funcName)) {
            throw new LexiException("Function '" + funcName + "' not defined");
        }
        
        Function func = functions.get(funcName);
        List<Object> args = new ArrayList<>();
        
        if (argsStr != null) {
            String[] argParts = argsStr.split(",");
            for (String arg : argParts) {
                args.add(evaluateExpression(arg.trim(), context));
            }
        }
        
        if (args.size() != func.parameters.size()) {
            throw new LexiException("Function '" + funcName + "' expects " + 
                func.parameters.size() + " arguments, got " + args.size());
        }
        
        // Create new context
        ExecutionContext funcContext = new ExecutionContext(context, funcName);
        
        // Bind parameters
        for (int i = 0; i < func.parameters.size(); i++) {
            funcContext.setVariable(func.parameters.get(i), args.get(i));
        }
        
        // Reset return state
        boolean previousReturnState = hasReturned;
        Object previousReturnValue = returnValue;
        hasReturned = false;
        returnValue = null;
        
        // Execute function
        executeBlock(func.body, 0, func.body.size(), funcContext);
        
        // Restore previous state
        hasReturned = previousReturnState;
        returnValue = previousReturnValue;
    }
    
    /**
     * Evaluate function call in expression context
     */
    private static Object evaluateFunctionCallExpression(String expr, ExecutionContext context) 
            throws LexiException {
        
        Pattern p = Pattern.compile(
            "(call|run function)\\s+(\\w+)(?:\\s+with\\s+(.+))?",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher m = p.matcher(expr);
        if (!m.find()) {
            throw new LexiException("Invalid function call syntax");
        }
        
        String funcName = m.group(2);
        String argsStr = m.group(3);
        
        // Execute function
        callFunction(funcName, argsStr, context);
        
        // Return the value
        if (returnValue == null) {
            throw new LexiException("Function '" + funcName + "' did not return a value");
        }
        
        Object result = returnValue;
        returnValue = null;
        hasReturned = false;
        
        return result;
    }
    
    // ============================================================
    // EXPRESSION EVALUATION
    // ============================================================
    
    private static Object evaluateExpression(String expr, ExecutionContext context) 
            throws LexiException {
        
        expr = expr.trim();
        
        // String literal
        if (expr.startsWith("\"") && expr.endsWith("\"")) {
            return expr.substring(1, expr.length() - 1);
        }
        
        // String concatenation
        if (expr.contains("+") && (expr.contains("\"") || hasStringVariable(expr, context))) {
            return evaluateStringConcatenation(expr, context);
        }
        
        // Numeric expression
        return evaluateNumericExpression(expr, context);
    }
    
    private static boolean hasStringVariable(String expr, ExecutionContext context) {
        String[] tokens = expr.split("[+\\-*/()%^]");
        for (String token : tokens) {
            token = token.trim();
            if (context.hasVariable(token)) {
                Object value = context.getVariable(token);
                if (value instanceof String) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private static String evaluateStringConcatenation(String expr, ExecutionContext context) 
            throws LexiException {
        
        StringBuilder result = new StringBuilder();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            
            if (c == '"') {
                if (inQuotes) {
                    result.append(current);
                    current = new StringBuilder();
                    inQuotes = false;
                } else {
                    if (current.length() > 0) {
                        String part = current.toString().trim();
                        if (!part.isEmpty() && !part.equals("+")) {
                            Object value = evaluateNumericExpression(part, context);
                            result.append(value);
                        }
                        current = new StringBuilder();
                    }
                    inQuotes = true;
                }
            } else if (c == '+' && !inQuotes) {
                if (current.length() > 0) {
                    String part = current.toString().trim();
                    if (!part.isEmpty()) {
                        if (context.hasVariable(part)) {
                            result.append(formatValue(context.getVariable(part)));
                        } else {
                            try {
                                result.append(evaluateNumericExpression(part, context));
                            } catch (Exception e) {
                                result.append(part);
                            }
                        }
                    }
                    current = new StringBuilder();
                }
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            String part = current.toString().trim();
            if (!part.isEmpty() && !part.equals("+")) {
                if (context.hasVariable(part)) {
                    result.append(formatValue(context.getVariable(part)));
                } else {
                    try {
                        result.append(evaluateNumericExpression(part, context));
                    } catch (Exception e) {
                        result.append(part);
                    }
                }
            }
        }
        
        return result.toString();
    }
    
    private static int evaluateNumericExpression(String expr, ExecutionContext context) 
            throws LexiException {
        
        expr = expr.trim();
        if (expr.isEmpty()) return 0;
        
        Parser parser = new Parser(expr, context);
        return parser.parseExpression();
    }
    
    // ============================================================
    // EXPRESSION PARSER (with proper precedence)
    // ============================================================
    
    static class Parser {
        private String expr;
        private int pos;
        private ExecutionContext context;
        
        Parser(String expr, ExecutionContext context) {
            this.expr = expr;
            this.pos = 0;
            this.context = context;
        }
        
        int parseExpression() throws LexiException {
            int result = parseTerm();
            
            while (pos < expr.length()) {
                skipWhitespace();
                if (pos >= expr.length()) break;
                
                char op = expr.charAt(pos);
                if (op == '+') {
                    pos++;
                    result += parseTerm();
                } else if (op == '-') {
                    pos++;
                    result -= parseTerm();
                } else {
                    break;
                }
            }
            
            return result;
        }
        
        int parseTerm() throws LexiException {
            int result = parseFactor();
            
            while (pos < expr.length()) {
                skipWhitespace();
                if (pos >= expr.length()) break;
                
                char op = expr.charAt(pos);
                if (op == '*') {
                    pos++;
                    result *= parseFactor();
                } else if (op == '/') {
                    pos++;
                    int divisor = parseFactor();
                    if (divisor == 0) {
                        throw new LexiException("Division by zero");
                    }
                    result /= divisor;
                } else if (op == '%') {
                    pos++;
                    result %= parseFactor();
                } else {
                    break;
                }
            }
            
            return result;
        }
        
        int parseFactor() throws LexiException {
            skipWhitespace();
            
            if (pos < expr.length() && expr.charAt(pos) == '-') {
                pos++;
                return -parsePrimary();
            }
            
            int result = parsePrimary();
            
            skipWhitespace();
            if (pos < expr.length() && expr.charAt(pos) == '^') {
                pos++;
                int exponent = parseFactor();
                result = (int) Math.pow(result, exponent);
            }
            
            return result;
        }
        
        int parsePrimary() throws LexiException {
            skipWhitespace();
            
            if (pos < expr.length() && expr.charAt(pos) == '(') {
                pos++;
                int result = parseExpression();
                skipWhitespace();
                if (pos < expr.length() && expr.charAt(pos) == ')') {
                    pos++;
                }
                return result;
            }
            
            StringBuilder sb = new StringBuilder();
            while (pos < expr.length()) {
                char c = expr.charAt(pos);
                if (Character.isLetterOrDigit(c) || c == '_') {
                    sb.append(c);
                    pos++;
                } else {
                    break;
                }
            }
            
            String token = sb.toString();
            if (token.isEmpty()) {
                throw new LexiException("Unexpected character at position " + pos);
            }
            
            try {
                return Integer.parseInt(token);
            } catch (NumberFormatException e) {
                if (!context.hasVariable(token)) {
                    throw new LexiException("Variable '" + token + "' not defined");
                }
                return toInt(context.getVariable(token));
            }
        }
        
        void skipWhitespace() {
            while (pos < expr.length() && Character.isWhitespace(expr.charAt(pos))) {
                pos++;
            }
        }
    }
    
    // ============================================================
    // BLOCK HANDLING
    // ============================================================
    
    private static int findBlockEnd(List<String> code, int startLine, int maxLine) {
        int depth = 0;
        
        for (int i = startLine; i < maxLine; i++) {
            String line = code.get(i).trim().toLowerCase();
            
            if (line.startsWith("if ") || line.startsWith("while ") || 
                line.startsWith("for ") || line.startsWith("function ") ||
                line.startsWith("define function")) {
                depth++;
            }
            
            if (line.equals("end") || line.equals("done") || line.equals("finish")) {
                depth--;
                if (depth == 0) {
                    return i;
                }
            }
        }
        
        return maxLine;
    }
    
    private static boolean isIfStatement(String line) {
        return line.trim().toLowerCase().startsWith("if ");
    }
    
    private static int handleIfBlock(List<String> code, int startLine, int maxLine, 
                                    ExecutionContext context) throws LexiException {
        
        String condition = code.get(startLine).substring(3).trim();
        boolean conditionMet = evaluateCondition(condition, context);
        
        int blockEnd = findBlockEnd(code, startLine, maxLine);
        int elseifLine = -1;
        int elseLine = -1;
        
        int depth = 0;
        for (int i = startLine; i <= blockEnd; i++) {
            String line = code.get(i).trim().toLowerCase();
            
            if (line.startsWith("if ") || line.startsWith("while ") || 
                line.startsWith("for ") || line.startsWith("function ")) {
                depth++;
            }
            
            if (line.equals("end")) {
                depth--;
            }
            
            if (depth == 1) {
                if (line.startsWith("elseif ") && elseifLine == -1) {
                    elseifLine = i;
                }
                if (line.equals("else") && elseLine == -1) {
                    elseLine = i;
                }
            }
        }
        
        if (conditionMet) {
            int ifEnd = (elseifLine != -1) ? elseifLine : (elseLine != -1) ? elseLine : blockEnd;
            executeBlock(code, startLine + 1, ifEnd, context);
        } else if (elseifLine != -1) {
            return handleIfBlock(code, elseifLine, maxLine, context);
        } else if (elseLine != -1) {
            executeBlock(code, elseLine + 1, blockEnd, context);
        }
        
        return blockEnd + 1;
    }
    
    private static boolean isWhileStatement(String line) {
        String lower = line.trim().toLowerCase();
        return lower.startsWith("while ") || lower.startsWith("repeat ");
    }
    
    private static int handleWhileBlock(List<String> code, int startLine, int maxLine, 
                                       ExecutionContext context) throws LexiException {
        
        String firstWord = code.get(startLine).trim().split("\\s+")[0].toLowerCase();
        String condition = code.get(startLine).substring(firstWord.length()).trim();
        
        int blockEnd = findBlockEnd(code, startLine, maxLine);
        
        while (evaluateCondition(condition, context)) {
            executeBlock(code, startLine + 1, blockEnd, context);
        }
        
        return blockEnd + 1;
    }
    
    private static boolean isForStatement(String line) {
        String lower = line.trim().toLowerCase();
        return lower.startsWith("for ") || lower.startsWith("loop ");
    }
    
    private static int handleForBlock(List<String> code, int startLine, int maxLine, 
                                     ExecutionContext context) throws LexiException {
        
        String line = code.get(startLine);
        Pattern p = Pattern.compile(
            "(?:for|loop)\\s+(\\w+)\\s+from\\s+(\\w+|\\d+)\\s+to\\s+(\\w+|\\d+)",
            Pattern.CASE_INSENSITIVE
        );
        
        Matcher m = p.matcher(line);
        if (!m.find()) {
            throw new LexiException("Invalid for loop syntax");
        }
        
        String varName = m.group(1);
        int start = toInt(evaluateExpression(m.group(2), context));
        int end = toInt(evaluateExpression(m.group(3), context));
        
        int blockEnd = findBlockEnd(code, startLine, maxLine);
        
        for (int i = start; i <= end; i++) {
            context.setVariable(varName, i);
            executeBlock(code, startLine + 1, blockEnd, context);
        }
        
        return blockEnd + 1;
    }
    
    private static boolean evaluateCondition(String condition, ExecutionContext context) 
            throws LexiException {
        
        condition = condition.trim();
        
        String[] operators = {"==", "!=", ">=", "<=", ">", "<"};
        
        for (String op : operators) {
            if (condition.contains(op)) {
                String[] parts = condition.split(Pattern.quote(op), 2);
                String leftStr = parts[0].trim();
                String rightStr = parts[1].trim();
                
                Object left = evaluateExpression(leftStr, context);
                Object right = evaluateExpression(rightStr, context);
                
                if (left instanceof Integer && right instanceof Integer) {
                    int l = (Integer) left;
                    int r = (Integer) right;
                    
                    switch (op) {
                        case "==": return l == r;
                        case "!=": return l != r;
                        case ">": return l > r;
                        case "<": return l < r;
                        case ">=": return l >= r;
                        case "<=": return l <= r;
                    }
                }
                
                if (left instanceof String || right instanceof String) {
                    String l = left.toString();
                    String r = right.toString();
                    
                    switch (op) {
                        case "==": return l.equals(r);
                        case "!=": return !l.equals(r);
                    }
                }
            }
        }
        
        return false;
    }
    
    private static boolean isBlockEnd(String line) {
        String lower = line.trim().toLowerCase();
        return lower.equals("end") || lower.equals("done") || lower.equals("finish");
    }
    
    // ============================================================
    // HELPER METHODS
    // ============================================================
    
    private static int toInt(Object value) throws LexiException {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new LexiException("Cannot convert string to number: " + value);
            }
        }
        throw new LexiException("Invalid number value: " + value);
    }
    
    private static String formatValue(Object value) {
        if (value instanceof ArrayList) {
            @SuppressWarnings("unchecked")
            ArrayList<Object> list = (ArrayList<Object>) value;
            if (list.isEmpty()) {
                return "[]";
            }
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(formatValue(list.get(i)));
            }
            sb.append("]");
            return sb.toString();
        }
        return value.toString();
    }
    
    private static void displayError(LexiException e) {
        System.err.println("\n❌ Error:");
        System.err.println(e.getMessage());
        System.err.println();
        
        if (debugMode && currentLine < program.size()) {
            System.err.println("At line:");
            int start = Math.max(0, currentLine - 2);
            int end = Math.min(program.size(), currentLine + 3);
            
            for (int i = start; i < end; i++) {
                String marker = (i == currentLine) ? ">>> " : "    ";
                System.err.println(marker + (i + 1) + ": " + program.get(i));
            }
            System.err.println();
        }
    }
}
