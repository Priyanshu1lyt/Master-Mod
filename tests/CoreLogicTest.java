import com.creatorshq.master.core.*;
import java.util.*;

/** Dependency-free tests for the Minecraft-independent core. Run: see BUILD_STATUS.md. */
public class CoreLogicTest {
    static int failed = 0;
    static void check(String name, boolean ok) { System.out.println((ok ? "PASS " : "FAIL ") + name); if (!ok) failed++; }

    public static void main(String[] a) {
        check("five themes registered", ThemeManager.all().size() == 5);
        check("default theme is midnight", ThemeManager.current().id().equals("midnight"));
        List<String> persisted = new ArrayList<>();
        ThemeManager.setPersistence(persisted::add);
        ThemeManager.select("emerald");
        check("select switches theme instantly", ThemeManager.current().id().equals("emerald"));
        check("select persists once", persisted.equals(List.of("emerald")));
        ThemeManager.apply("does-not-exist");
        check("unknown theme falls back", ThemeManager.current().id().equals("midnight"));
        Set<Integer> accents = new HashSet<>();
        for (Theme t : ThemeManager.all()) accents.add(t.accent());
        check("themes have distinct accents", accents.size() == 5);

        Map<String, Integer> keys = new LinkedHashMap<>();
        keys.put("gamemode", 71); keys.put("enchant", 71); keys.put("give", 71); keys.put("tp", 80); keys.put("none", -1); keys.put("none2", -1);
        Map<Integer, List<String>> c = KeyConflicts.find(keys);
        check("detects 3-way G conflict", c.size() == 1 && c.get(71).size() == 3);
        check("unbound keys never conflict", !KeyConflicts.conflicts(keys, "none"));
        check("unique key no conflict", !KeyConflicts.conflicts(keys, "tp"));
        keys.put("enchant", 72); keys.put("give", 74);
        check("resolved defaults -> no conflicts", KeyConflicts.find(keys).isEmpty());

        HqConfig cfg = new HqConfig();
        cfg.theme = "bogus"; cfg.uiScalePercent = 999; cfg.moduleEnabled = null; cfg.schemaVersion = 0;
        cfg.moduleSettings = new LinkedHashMap<>(); cfg.moduleSettings.put("future-module", Map.of("k", "v"));
        int fixes = ConfigValidator.sanitize(cfg);
        check("sanitize repairs fields", fixes >= 4 && cfg.theme.equals("midnight") && cfg.uiScalePercent == 150 && cfg.moduleEnabled != null);
        check("sanitize keeps unknown module data", cfg.moduleSettings.containsKey("future-module"));
        check("unknownModules reports it", ConfigValidator.unknownModules(cfg, Set.of("tp")).contains("future-module"));

        System.out.println(failed == 0 ? "ALL PASSED" : failed + " FAILED");
        System.exit(failed == 0 ? 0 : 1);
    }
}
