package moe.sqwatermark.groovy_test;

import com.mojang.logging.LogUtils;
import groovy.lang.Binding;
import groovy.lang.Script;
import groovy.util.GroovyScriptEngine;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class GroovyManager {

    private static final Logger LOGGER = LogUtils.getLogger();

    // 脚本文件夹放在src/main文件内，这样就可以直接使用IDEA的代码补全调用模组所能调用的代码了
    public static final File GROOVY_DIR = new File("../src/main/groovy");

    // 缓存所有脚本
    public static final Map<String, Script> SCRIPT_MAP = new HashMap<>();

    // 脚本引擎
    public static final GroovyScriptEngine GROOVY_SCRIPT_ENGINE;

    // 加载Groovy脚本引擎
    static {
        try {
            GROOVY_SCRIPT_ENGINE = new GroovyScriptEngine(new URL[]{GROOVY_DIR.toURI().toURL()}, GroovyManager.class.getClassLoader());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
        // 加载脚本文件夹下的所有文件
        loadAllScripts();
        // 监听文件变化，虽然野蛮但是也省事，也可以注册一个指令以重载脚本
        createMonitor();
    }

    public static void loadAllScripts() {
        if (GROOVY_DIR.isDirectory()) {
            File[] subFiles = GROOVY_DIR.listFiles();
            if (subFiles != null) {
                for (File file : subFiles) {
                    loadFile(file);
                }
            }
        }
    }

    private static void loadFile(File file) {
        try {
            // 从文件创建脚本
            Script script = GROOVY_SCRIPT_ENGINE.createScript(file.getName(), new Binding());
            Script previous = SCRIPT_MAP.put(file.getName(), script);
            if (previous == null) {
                LOGGER.info("成功加载脚本 {}", file.getName());
            } else {
                LOGGER.info("成功重载脚本 {}", file.getName());
            }
        } catch (Exception e) {
            LOGGER.warn("加载脚本 {} 失败", file.getName());
            e.printStackTrace();
        }
    }

    /**
     * 监听脚本文件变化，每秒更新一次，如有变化，则重新加载该脚本
     */
    private static void createMonitor() {
        FileAlterationMonitor monitor = new FileAlterationMonitor(1000);

        FileAlterationObserver observer = new FileAlterationObserver(GROOVY_DIR);
        monitor.addObserver(observer);
        observer.addListener(new FileAlterationListenerAdaptor() {
            @Override
            public void onFileChange(File file) {
                loadFile(file);
            }

            @Override
            public void onFileCreate(File file) {
                loadFile(file);
            }

            @Override
            public void onFileDelete(File file) {
                SCRIPT_MAP.remove(file.getName());
            }
        });

        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 执行脚本方法
     * @param fileName 脚本文件名
     * @param function 脚本函数
     * @param args 入参
     */
    public static Object invoke(String fileName, String function, Object... args) {
        Script script = GroovyManager.SCRIPT_MAP.get(fileName);
        if (script != null) {
            try {
                return script.invokeMethod(function, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}