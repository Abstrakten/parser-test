package parser.test;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.*;
import java.util.concurrent.Callable;

// @@@
public class App {

    public static void main(String[] args) {
        int exitCode = new CommandLine(new CodeReader()).execute(args);
        System.exit(exitCode);
    }
}

@Command(name = "CodeReader", mixinStandardHelpOptions = true, description = "Models stuff")
// @@@
class CodeReader implements Callable<Integer> {

    @Parameters(index = "0", description = "The java program to read")
    private File file;

    @Option(names = { "-p", "--prefix" }, description = "comment prefix")
    private String prefix = "@@@";

    @Override
    public Integer call() throws Exception {

        var elements = ReadFile(file);
        WriteFile(elements);

        return 0;
    }

    private List<ModelElement> ReadFile(File input) throws Exception {

        var lineReader = new LineReader();

        String fullPrefix = "// ".concat(this.prefix);
        System.out.println("fullPrefix is: " + fullPrefix);

        List<ModelElement> elements = new ArrayList<ModelElement>();

        try (var scanner = new Scanner(input)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();

                if (line.stripLeading().startsWith(fullPrefix)) {
                    String lineToRead = scanner.nextLine();

                    // System.out.println("lineToRead is: " + lineToRead);

                    ModelElement newElement = lineReader.ReadLine(lineToRead)
                            .orElseThrow(() -> new Exception("Unknown element in line: ".concat(line)));
                    elements.add(newElement);
                }
            }
        }

        return elements;

    }

    private void WriteFile(List<ModelElement> elements) {

        System.out.println("\nWriting result:");

        elements.stream()
                .map(element -> element.name + " this is a " + element.type.toString())
                .forEach(System.out::println);
    }

    // @@@
    class LineReader {

        private Map<String, ElementType> types = Map.of(
                "public class", ElementType.PUBLIC_CLASS,
                "class", ElementType.CLASS);

        public Optional<ModelElement> ReadLine(String line) {
            var typeIterator = types.keySet().iterator();

            Optional<ModelElement> result = Optional.empty();

            while (typeIterator.hasNext()) {
                var type = typeIterator.next();
                if (line.stripLeading().startsWith(type)) {
                    var lineType = types.get(type);
                    var lineName = line.stripLeading().substring(type.length()).trim().split(" ")[0];
                    result = Optional.of(new ModelElement(lineType, lineName));
                }
            }

            return result;
        }

    }

}

class ModelElement {

    public final ElementType type;
    public final String name;

    public ModelElement(ElementType type, String name) {
        this.type = type;
        this.name = name;
    }

}

enum ElementType {
    PUBLIC_CLASS,
    CLASS,
}