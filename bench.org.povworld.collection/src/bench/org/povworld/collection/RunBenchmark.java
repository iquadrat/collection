package bench.org.povworld.collection;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jbenchx.BenchmarkContext;
import org.jbenchx.BenchmarkParameters;
import org.jbenchx.BenchmarkRunner;
import org.jbenchx.IBenchmarkContext;
import org.jbenchx.monitor.ConsoleProgressMonitor;
import org.jbenchx.util.StringUtil;

public class RunBenchmark {
    
    private static final String[] TAGS = {};
    
    public static void main(String[] benchmarks) throws Exception {
        BenchmarkRunner runner = new BenchmarkRunner();
        for (String benchmark: benchmarks) {
            runner.add(Class.forName(benchmark));
        }
        
        IBenchmarkContext context = BenchmarkContext.create(new ConsoleProgressMonitor());
        BenchmarkParameters params = BenchmarkParameters.getDefaults();
        
        List<Pattern> patterns = new ArrayList<Pattern>();
        for (String tag: TAGS) {
            patterns.add(StringUtil.wildCardToRegexpPattern(tag));
        }
        if (patterns.isEmpty()) {
            patterns = BenchmarkContext.RUN_ALL;
        }
        
        context = new BenchmarkContext(context.getProgressMonitor(), context.getSystemInfo(), patterns, params);
        
        runner.run(context);
    }
    
}
