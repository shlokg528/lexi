# Lexi Programming Language

**Version 1.0**

A natural language programming language that feels like writing English.

```lexi
create a number called score with value 90
increase score by 5
display "Score: " + score
```

## Features

- ✨ **Natural Language Syntax** - Write code that reads like English
- 🔢 **Expression Evaluation** - Proper operator precedence and parentheses
- 🎯 **Functions with Return Values** - Reusable code blocks
- 🔄 **Loops & Conditionals** - Full control flow support
- 📦 **Arrays** - Store collections of data
- 💬 **Interactive REPL** - Test code immediately
- 🐛 **Helpful Error Messages** - Know exactly what went wrong
- 🎨 **Clean Architecture** - Beginner-friendly, well-documented code

## Quick Start

### Installation

1. Download `Lexi.java`
2. Compile: `javac Lexi.java`
3. Run: `java Lexi`

### Run the Showcase Example

```bash
java Lexi finance_calculator.lexi
```

## Language Guide

See [LANGUAGE_GUIDE.md](LANGUAGE_GUIDE.md) for complete documentation.

### Variables

```lexi
create a number called score with value 90
set x to 5
increase counter by 1
```

### Functions with Returns

```lexi
function square n
    return n * n
end

set result to call square with 5
```

### Example Programs

See `examples/` directory for more!

## Project Structure

```
lexi/
├── src/
│   └── Lexi.java              # Main interpreter
├── examples/
│   ├── finance_calculator.lexi # Showcase program
│   └── ...
└── docs/
    └── README.md
```

## License

MIT License

---

**Lexi** - Programming in Plain English
