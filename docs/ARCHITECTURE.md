# Lexi Architecture Documentation

## Overview

Lexi is a natural language programming interpreter built with clean architecture and beginner-friendly design principles.

## Core Architecture

```
┌─────────────────────────────────────────────┐
│           REPL / File Loader                │
│  (User Interface & Input Management)        │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│         Pattern Matcher                     │
│  (Natural Language → Commands)              │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│         Block Executor                      │
│  (Structured Execution)                     │
└──────────────────┬──────────────────────────┘
                   │
        ┌──────────┴──────────┐
        ▼                     ▼
┌───────────────┐    ┌────────────────┐
│   Expression  │    │   Control Flow │
│    Parser     │    │   (if/while)   │
└───────────────┘    └────────────────┘
        │                     │
        └──────────┬──────────┘
                   ▼
        ┌──────────────────────┐
        │  Execution Context   │
        │  (Variable Scopes)   │
        └──────────────────────┘
```

## Key Components

### 1. Execution Context

**Purpose**: Manages variable scopes (local and global)

**Design**: Chain of responsibility pattern

```java
class ExecutionContext {
    Map<String, Object> localVariables;
    ExecutionContext parent;
    String scopeName;
    
    // Look up chain: local -> parent -> global
    Object getVariable(String name) {
        if (localVariables.containsKey(name))
            return localVariables.get(name);
        if (parent != null)
            return parent.getVariable(name);
        return globalVariables.get(name);
    }
}
```

**Scope Chain Example**:
```
Global Context
    │
    ├─ Function "outer" Context
    │      │
    │      └─ Function "inner" Context
    │             │
    │             └─ Variable lookup travels up chain
```

### 2. Pattern Matcher

**Purpose**: Convert natural language to executable commands

**Design**: Strategy pattern with regex

Each pattern has its own `tryXXX()` method:

```java
private static boolean tryCreateVariable(String line, ExecutionContext ctx) {
    Pattern p = Pattern.compile(
        "create\\s+a\\s+(?:number|variable)\\s+called\\s+(\\w+)...",
        Pattern.CASE_INSENSITIVE
    );
    
    Matcher m = p.matcher(line);
    if (m.matches()) {
        String varName = m.group(1);
        String valueExpr = m.group(2);
        // Execute...
        return true;
    }
    return false;
}
```

**Pattern Matching Flow**:
```
Input Line
    │
    ├─ tryCreateVariable()    → matches? Execute & return true
    ├─ trySetVariable()       → matches? Execute & return true
    ├─ tryIncreaseVariable()  → matches? Execute & return true
    ├─ tryDisplay()           → matches? Execute & return true
    └─ ... more patterns
    
If no match → throw "Unknown command"
```

### 3. Expression Parser

**Purpose**: Evaluate arithmetic expressions with proper precedence

**Design**: Recursive descent parser

**Grammar**:
```
Expression → Term (('+' | '-') Term)*
Term       → Factor (('*' | '/' | '%') Factor)*
Factor     → Primary ('^' Factor)?
Primary    → Number | Variable | '(' Expression ')'
```

**Parser Structure**:

```java
class Parser {
    // Entry point - lowest precedence
    int parseExpression() {
        int result = parseTerm();
        while (peek() == '+' or '-')
            result = result +/- parseTerm();
        return result;
    }
    
    // Medium precedence
    int parseTerm() {
        int result = parseFactor();
        while (peek() == '*' or '/' or '%')
            result = result op parseFactor();
        return result;
    }
    
    // High precedence
    int parseFactor() {
        int result = parsePrimary();
        if (peek() == '^')
            result = result ^ parseFactor();  // Right-associative
        return result;
    }
    
    // Highest precedence
    int parsePrimary() {
        if (peek() == '(')
            return parseExpression();
        return parseNumberOrVariable();
    }
}
```

**Example Trace**: `(10 + 5) * 2`

```
parseExpression()
  └─ parseTerm()
      └─ parseFactor()
          └─ parsePrimary()
              └─ see '(' → parseExpression()
                  └─ parseTerm() → parsePrimary() → 10
                  └─ see '+' 
                  └─ parseTerm() → parsePrimary() → 5
                  └─ return 15
              └─ see ')' → return 15
      └─ see '*'
      └─ parseFactor() → parsePrimary() → 2
      └─ return 15 * 2 = 30
```

### 4. Block Executor

**Purpose**: Execute blocks of code with proper nesting

**Design**: Structured execution (not instruction pointer jumping)

**Key Insight**: Use recursion instead of jumps

```java
void executeBlock(List<String> code, int start, int end, ExecutionContext ctx) {
    for (int i = start; i < end; i++) {
        String line = code.get(i);
        
        if (isIfStatement(line)) {
            i = handleIfBlock(code, i, end, ctx);
        } else if (isWhileStatement(line)) {
            i = handleWhileBlock(code, i, end, ctx);
        } else {
            executeStatement(line, ctx);
        }
    }
}
```

**Nested Block Handling**:

```
if x > 5                    ← Start block, depth=1
    while y < 10            ← Start block, depth=2
        display y           ← Execute
        increase y          ← Execute
    end                     ← End block, depth=1
    display "done"          ← Execute
end                         ← End block, depth=0 (found!)
```

**Depth Tracking Algorithm**:

```java
int findBlockEnd(List<String> code, int start, int max) {
    int depth = 0;
    
    for (int i = start; i < max; i++) {
        if (isBlockStart(code.get(i)))
            depth++;
        if (isBlockEnd(code.get(i))) {
            depth--;
            if (depth == 0)
                return i;  // Found matching end!
        }
    }
    return max;
}
```

### 5. Function Manager

**Purpose**: Store and execute functions with parameters

**Design**: Function as first-class objects

```java
class Function {
    String name;
    List<String> parameters;
    List<String> body;
}

Map<String, Function> functions = new HashMap<>();
```

**Function Call Flow**:

```
1. Parse: "call square with 5"
   ├─ Extract: funcName="square", args=[5]
   
2. Lookup function in map
   └─ Error if not found
   
3. Create new ExecutionContext
   ├─ Parent: calling context
   ├─ Scope name: "square"
   
4. Bind parameters to arguments
   ├─ parameters[0]="n" → 5
   
5. Execute function body
   └─ In new context (local scope)
   
6. Handle return value
   └─ Store in global returnValue
   
7. Return to caller
```

**Return Value Mechanism**:

```java
// Global state
static Object returnValue = null;
static boolean hasReturned = false;

// In function:
return n * n
    ↓
returnValue = evaluate("n * n")
hasReturned = true
    ↓
Exit function execution

// In caller:
set result to call square with 5
    ↓
Execute function (sets returnValue)
    ↓
result = returnValue
returnValue = null
hasReturned = false
```

### 6. Error Handler

**Purpose**: Provide meaningful error messages with context

**Design**: Custom exception with line tracking

```java
class LexiException extends Exception {
    LexiException(String message) {
        super("Line " + (currentLine + 1) + ": " + message);
    }
}

// Usage:
if (!context.hasVariable(varName)) {
    throw new LexiException("Variable '" + varName + "' not defined");
}
```

**Error Display**:

```
❌ Error:
Line 12: Variable 'score' not defined

At line:
10: set x to 5
11: set y to 10
>>> 12: display score    ← Error here
13: end
```

## Data Flow

### Variable Assignment

```
Input: "set score to 90"
    ↓
Pattern Match: trySetVariable()
    ↓
Extract: varName="score", expr="90"
    ↓
Evaluate Expression: 90
    ↓
Store in Context: context.setVariable("score", 90)
    ↓
Done
```

### Function Call with Return

```
Input: "set result to call square with 5"
    ↓
Pattern Match: trySetVariable()
    ↓
Detect: contains "call"
    ↓
Parse Function Call: name="square", args=[5]
    ↓
Create Context: new ExecutionContext(parent=current)
    ↓
Bind Parameters: n=5
    ↓
Execute Body:
    "return n * n"
        ↓
    Evaluate: 5 * 5 = 25
        ↓
    returnValue = 25
    hasReturned = true
    ↓
Return to Caller: value=25
    ↓
Store: context.setVariable("result", 25)
    ↓
Done
```

### Nested Loop Execution

```
Input:
for i from 1 to 3
    for j from 1 to 2
        display i * j
    end
end

Execution:
executeBlock([for i...], 0, 5, context)
    ↓
Line 0: "for i from 1 to 3"
    ↓
handleForBlock(start=0)
    ├─ Set i=1
    ├─ executeBlock([for j...], 1, 4, context)
    │   ├─ Line 1: "for j from 1 to 2"
    │   ├─ handleForBlock(start=1)
    │   │   ├─ Set j=1
    │   │   ├─ executeBlock([display...], 2, 3, context)
    │   │   │   └─ display 1*1=1
    │   │   ├─ Set j=2
    │   │   └─ executeBlock([display...], 2, 3, context)
    │   │       └─ display 1*2=2
    │   └─ Return to outer loop
    ├─ Set i=2
    ├─ executeBlock([for j...], 1, 4, context)
    │   └─ ... repeat inner loop
    └─ Set i=3
        └─ executeBlock([for j...], 1, 4, context)
            └─ ... repeat inner loop
```

## Design Decisions

### Why Recursive Descent Parser?

**Pros**:
- ✅ Beginner-friendly (matches grammar directly)
- ✅ No external dependencies
- ✅ Easy to debug
- ✅ Natural precedence handling

**Cons**:
- ❌ Not as fast as table-driven parsers
- ❌ Limited to simple grammars

**Decision**: For Lexi's use case (educational, small programs), simplicity and readability outweigh performance concerns.

### Why Structured Block Execution?

**Alternative**: Instruction pointer jumping

```java
// OLD WAY (jumping)
int ip = 0;
while (ip < program.size()) {
    if (isIf()) {
        if (condition) {
            ip++;  // Execute if block
        } else {
            ip = findElse();  // Jump to else
        }
    }
    ip++;
}
```

**Problems**:
- Hard to debug (jumps everywhere)
- Difficult to track scope
- Error-prone for nested blocks

**Our Way**: Recursive execution

```java
// NEW WAY (structured)
void executeBlock(code, start, end, context) {
    for (line in code[start:end]) {
        if (isIf()) {
            executeBlock(ifBody, ..., context);
        }
    }
}
```

**Benefits**:
- ✅ Clear execution flow
- ✅ Natural scope management
- ✅ Call stack shows nesting
- ✅ Easy to extend

### Why Regex for Patterns?

**Alternative**: Hand-written lexer/parser

**Pros of Regex**:
- ✅ Concise pattern definition
- ✅ Built-in to Java
- ✅ Easy to modify patterns
- ✅ Good for natural language

**Cons**:
- ❌ Can't handle complex nested structures
- ❌ Hard to debug complex patterns

**Decision**: Regex perfect for natural language commands. Use structured parser only for expressions.

## Performance Considerations

### Optimization Opportunities

1. **Expression Caching**: Cache parsed expressions
2. **Pattern Compilation**: Compile regex patterns once (static final)
3. **Lazy Evaluation**: Don't parse until needed
4. **Symbol Table**: Use hash maps for O(1) variable lookup

### Current Bottlenecks

1. **Line-by-line execution**: No bytecode compilation
2. **String operations**: Frequent string splitting/joining
3. **Block searching**: Linear search for matching 'end'

### When to Optimize

**Don't optimize now if**:
- Programs are < 1000 lines
- Execution time < 1 second
- Focus is on learning/teaching

**Optimize when**:
- Programs exceed 10,000 lines
- Performance becomes noticeable
- Production deployment needed

## Extension Points

### Adding New Data Types

```java
// 1. Create type class
class LexiList {
    List<Object> items;
}

// 2. Add to context
Object getVariable(String name) {
    // ... handle LexiList
}

// 3. Add operations
private static boolean tryListOperation(String line, ExecutionContext ctx) {
    // Pattern match list operations
}
```

### Adding New Control Structures

```java
// Example: switch statement

// 1. Add detection
private static boolean isSwitchStatement(String line) {
    return line.trim().toLowerCase().startsWith("switch ");
}

// 2. Add handler
private static int handleSwitchBlock(List<String> code, int start, ...) {
    // Parse cases
    // Execute matching case
    // Return end position
}

// 3. Add to executeBlock()
if (isSwitchStatement(line)) {
    i = handleSwitchBlock(code, i, end, context);
}
```

### Adding Standard Library

```java
// Create built-in functions
static {
    // Math functions
    functions.put("sqrt", new BuiltinFunction("sqrt", 1) {
        Object call(List<Object> args) {
            return (int) Math.sqrt((Integer) args.get(0));
        }
    });
    
    // String functions
    functions.put("length", new BuiltinFunction("length", 1) {
        Object call(List<Object> args) {
            return args.get(0).toString().length();
        }
    });
}
```

## Testing Strategy

### Unit Tests

```java
@Test
public void testExpressionEvaluation() {
    ExecutionContext ctx = new ExecutionContext(null, "test");
    assertEquals(30, evaluateNumericExpression("(10 + 5) * 2", ctx));
    assertEquals(20, evaluateNumericExpression("10 + 5 * 2", ctx));
}

@Test
public void testVariableScopes() {
    ExecutionContext global = new ExecutionContext(null, "global");
    global.setVariable("x", 10);
    
    ExecutionContext local = new ExecutionContext(global, "local");
    local.setVariable("y", 20);
    
    assertEquals(10, local.getVariable("x"));  // From parent
    assertEquals(20, local.getVariable("y"));  // From local
}
```

### Integration Tests

```lexi
# test_integration.lexi

# Test: function with return
function add x y
    return x + y
end

set result to call add with 10, 20
if result != 30
    display "FAILED: function return"
end

# Test: nested loops
set count to 0
for i from 1 to 3
    for j from 1 to 2
        increase count
    end
end

if count != 6
    display "FAILED: nested loops"
end

display "All tests passed!"
```

## Future Architecture

### Planned Improvements

1. **Bytecode Compilation**
   - Parse once, execute many times
   - Store compiled bytecode

2. **Type System**
   - Strong typing with inference
   - Type checking at parse time

3. **Module System**
   - Import/export functions
   - Namespace management

4. **Garbage Collection**
   - Currently relies on Java GC
   - Could add reference counting

5. **JIT Compilation**
   - Compile hot code paths
   - Use Java's JIT features

---

This architecture provides a solid foundation for learning, teaching, and extending Lexi!
