# Build Instructions

## Prerequisites
- Java Development Kit (JDK) 8 or higher
- Maven 3.6 or higher
- Internet connection (for downloading dependencies)

## Building the Plugin

1. **Open Terminal/Command Prompt** in the project directory

2. **Clean and Build:**
   ```bash
   mvn clean package
   ```

3. **Output Location:**
   The compiled JAR file will be located at:
   ```
   target/NameColor-1.0.jar
   ```

## Maven Commands

- `mvn clean` - Remove previous build files
- `mvn compile` - Compile the source code
- `mvn package` - Create the JAR file
- `mvn clean package` - Clean and build in one step

## Troubleshooting

### Maven Not Found
- Install Maven from https://maven.apache.org/download.cgi
- Add Maven to your system PATH

### Java Version Issues
- Ensure JDK 8 or higher is installed
- Check version: `java -version`
- Set JAVA_HOME environment variable

### Dependency Download Failures
- Check internet connection
- Maven will download Spigot API from the Spigot repository
- First build may take longer due to downloads

## Testing

After building:
1. Copy `target/NameColor-1.0.jar` to your test server's `plugins/` folder
2. Start or reload your server
3. Test with `/namecolor red` (or any other color)
4. Verify config generation in `plugins/NameColor/`

## Development

To modify the plugin:
1. Edit source files in `src/main/java/`
2. Update `config.yml` in `src/main/resources/` if needed
3. Rebuild with `mvn clean package`
4. Replace the JAR on your test server
