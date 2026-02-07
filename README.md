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

Copyright (c) 2026 shlokg528

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

---

**Lexi** - Programming in Plain English
