import java.util.spi.ToolProvider;

// Usa a ferramenta jar do próprio JDK mesmo quando somente java/javac estão no PATH.
class PackageJar {
    public static void main(String[] args) {
        ToolProvider jar = ToolProvider.findFirst("jar").orElseThrow(
                () -> new IllegalStateException("Instale um JDK completo 17 ou superior."));
        System.exit(jar.run(System.out, System.err, args));
    }
}
