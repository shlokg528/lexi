# Lexi Project Structure Guide

## Recommended Directory Structure

```
lexi/
│
├── src/
│   └── Lexi.java                    # Main interpreter
│
├── examples/
│   ├── hello.lexi                   # Hello World
│   ├── calculator.lexi              # Basic calculator
│   ├── finance_calculator.lexi      # Full showcase
│   ├── grade_calculator.lexi        # Student grades
│   ├── fibonacci.lexi               # Fibonacci sequence
│   └── game_guessnumber.lexi        # Number guessing game
│
├── docs/
│   ├── README.md                    # Main documentation
│   ├── LANGUAGE_GUIDE.md            # Language reference
│   ├── ARCHITECTURE.md              # Technical details
│   ├── TUTORIAL.md                  # Step-by-step guide
│   └── API.md                       # Extension API
│
├── tests/
│   ├── test_basic.lexi              # Basic features
│   ├── test_functions.lexi          # Function tests
│   ├── test_expressions.lexi        # Expression tests
│   └── test_errors.lexi             # Error handling
│
└── tools/
    ├── vscode/
    │   └── lexi.tmLanguage.json     # Syntax highlighting
    └── syntax/
        └── lexi-mode.el             # Emacs mode (future)
```

## Setting Up Your Project

### Step 1: Create Directory Structure

```bash
mkdir -p lexi/{src,examples,docs,tests,tools/vscode}
cd lexi
```

### Step 2: Place Files

```bash
# Main interpreter
mv Lexi.java src/

# Example programs
mv finance_calculator.lexi examples/
mv hello.lexi examples/  # Create other examples

# Documentation
mv README.md docs/
mv LANGUAGE_GUIDE.md docs/
```

### Step 3: Compile

```bash
cd src
javac Lexi.java
```

### Step 4: Run Examples

```bash
# From src directory
java Lexi ../examples/finance_calculator.lexi

# Or set up a script:
alias lexi='java -cp /path/to/lexi/src Lexi'
lexi ../examples/hello.lexi
```

## Package Structure (for larger projects)

If you want to organize Lexi as a proper Java package:

```
lexi/
└── src/
    └── com/
        └── yourname/
            └── lexi/
                ├── Lexi.java              # Main class
                ├── core/
                │   ├── Parser.java        # Expression parser
                │   ├── Executor.java      # Statement executor
                │   └── Context.java       # Execution context
                ├── functions/
                │   └── FunctionManager.java
                ├── patterns/
                │   └── PatternMatcher.java
                └── repl/
                    └── REPL.java          # Interactive mode
```

## Build Scripts

### Unix/Linux/Mac (build.sh)

```bash
#!/bin/bash
echo "Building Lexi..."
cd src
javac Lexi.java
echo "Build complete!"
```

### Windows (build.bat)

```batch
@echo off
echo Building Lexi...
cd src
javac Lexi.java
echo Build complete!
```

### Run Script (run.sh)

```bash
#!/bin/bash
if [ -z "$1" ]; then
    # Interactive mode
    java -cp src Lexi
else
    # File execution
    java -cp src Lexi "$1"
fi
```

## IDE Setup

### IntelliJ IDEA

1. Create new Java project
2. Set `src/` as source directory
3. Mark `examples/` as resources
4. Run configuration:
   - Main class: `Lexi`
   - Program arguments: `../examples/finance_calculator.lexi`

### VS Code

Create `.vscode/launch.json`:

```json
{
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Launch Lexi",
            "request": "launch",
            "mainClass": "Lexi",
            "projectName": "lexi"
        },
        {
            "type": "java",
            "name": "Run Lexi File",
            "request": "launch",
            "mainClass": "Lexi",
            "args": ["${file}"]
        }
    ]
}
```

### Eclipse

1. New Java Project → "lexi"
2. Add `src/` folder
3. Right-click Lexi.java → Run As → Java Application
4. Run Configurations → Arguments → Add file path

## Testing

### Create Test Suite

`tests/run_tests.sh`:

```bash
#!/bin/bash

echo "Running Lexi Test Suite"
echo "======================="

for test in tests/*.lexi; do
    echo ""
    echo "Running: $test"
    java -cp src Lexi "$test"
    if [ $? -eq 0 ]; then
        echo "✓ PASSED"
    else
        echo "✗ FAILED"
    fi
done
```

### Example Test File

`tests/test_basic.lexi`:

```lexi
# Test basic features
create a number called x with value 10
display x

set y to 20
display y

increase x by 5
display x

if x == 15
    display "Test passed!"
end
```

## Distribution

### Create Release Package

```bash
#!/bin/bash
VERSION="1.0"
PACKAGE="lexi-$VERSION"

mkdir -p release/$PACKAGE/{src,examples,docs}

# Copy files
cp src/Lexi.java release/$PACKAGE/src/
cp examples/*.lexi release/$PACKAGE/examples/
cp docs/*.md release/$PACKAGE/docs/

# Create README
cat > release/$PACKAGE/README.txt << EOF
Lexi Programming Language v$VERSION

Installation:
1. cd src
2. javac Lexi.java
3. java Lexi ../examples/hello.lexi

Documentation: See docs/README.md
EOF

# Create archive
cd release
zip -r $PACKAGE.zip $PACKAGE/
tar czf $PACKAGE.tar.gz $PACKAGE/

echo "Release packages created:"
echo "  release/$PACKAGE.zip"
echo "  release/$PACKAGE.tar.gz"
```

## Git Setup

`.gitignore`:

```
# Compiled files
*.class
*.jar

# IDE files
.idea/
.vscode/
*.iml
.project
.classpath
.settings/

# OS files
.DS_Store
Thumbs.db

# Build output
build/
out/
bin/
```

`README.md` for GitHub:

```markdown
# Lexi Programming Language

Natural language programming that feels like writing English.

## Quick Start

\`\`\`bash
javac src/Lexi.java
java -cp src Lexi examples/hello.lexi
\`\`\`

## Example

\`\`\`lexi
create a number called score with value 90
increase score by 5
display "Final score: " + score
\`\`\`

See [docs/README.md](docs/README.md) for full documentation.
```

## Deployment Options

### 1. JAR File

Create runnable JAR:

```bash
# Compile
javac -d build src/Lexi.java

# Create manifest
echo "Main-Class: Lexi" > manifest.txt

# Package
jar cfm lexi.jar manifest.txt -C build .

# Run
java -jar lexi.jar examples/hello.lexi
```

### 2. Shell Script Wrapper

`/usr/local/bin/lexi`:

```bash
#!/bin/bash
java -cp /opt/lexi/Lexi.class Lexi "$@"
```

Make executable:
```bash
chmod +x /usr/local/bin/lexi
```

Use anywhere:
```bash
lexi myprogram.lexi
```

### 3. Docker Container

`Dockerfile`:

```dockerfile
FROM openjdk:11-jre-slim

WORKDIR /app
COPY src/Lexi.class .

ENTRYPOINT ["java", "Lexi"]
CMD []
```

Build and run:
```bash
docker build -t lexi .
docker run -v $(pwd)/examples:/examples lexi /examples/hello.lexi
```

## Documentation Workflow

1. Write code with comments
2. Extract documentation: Javadoc for API
3. Write examples in `examples/`
4. Create tutorials in `docs/`
5. Generate website (optional): Use MkDocs or Jekyll

## Contribution Workflow

1. Fork repository
2. Create feature branch: `git checkout -b feature-name`
3. Add tests in `tests/`
4. Add example in `examples/`
5. Update docs in `docs/`
6. Submit pull request

## Versioning

Follow Semantic Versioning (semver.org):

- `1.0.0` - Initial release
- `1.0.1` - Bug fixes
- `1.1.0` - New features (backward compatible)
- `2.0.0` - Breaking changes

Tag releases:
```bash
git tag -a v1.0.0 -m "Version 1.0.0"
git push origin v1.0.0
```

## Maintenance Checklist

- [ ] Keep examples up-to-date
- [ ] Test on multiple Java versions
- [ ] Update documentation for new features
- [ ] Monitor and fix reported issues
- [ ] Regular security updates
- [ ] Performance optimization
- [ ] Community engagement

---

This structure provides a solid foundation for developing and maintaining Lexi!
