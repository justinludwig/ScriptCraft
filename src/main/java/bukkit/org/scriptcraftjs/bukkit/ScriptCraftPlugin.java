package org.scriptcraftjs.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ScriptCraftPlugin extends JavaPlugin
{
    public boolean canary = false;
    public boolean bukkit = true;
    // right now all ops share the same JS context/scope
    // need to look at possibly having context/scope per operator
    //protected Map<CommandSender,ScriptCraftEvaluator> playerContexts = new HashMap<CommandSender,ScriptCraftEvaluator>();
    private String NO_JAVASCRIPT_MESSAGE = "No JavaScript Engine available. ScriptCraft will not work without Javascript.";
    protected ScriptEngine engine = null;

    @Override public void onEnable()
    {
        Thread currentThread = Thread.currentThread();
        ClassLoader previousClassLoader = currentThread.getContextClassLoader();
        currentThread.setContextClassLoader(new ContextClassLoader());
        try {
            ScriptEngineManager factory = new ScriptEngineManager();
            this.engine = factory.getEngineByName("JavaScript");
            if (this.engine == null) {
                this.getLogger().severe(NO_JAVASCRIPT_MESSAGE);
            } else {
                // https://docs.oracle.com/en/graalvm/enterprise/21/docs/reference-manual/js/ScriptEngine/
                Bindings bindings = this.engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put("polyglot.js.allowAllAccess", true);
                bindings.put("polyglot.js.nashorn-compat", true);

                Invocable inv = (Invocable) this.engine;
                this.engine.eval(new InputStreamReader(this.getResource("boot.js")));
                inv.invokeFunction("__scboot", this, engine);
            }
        } catch (Exception e) {
            e.printStackTrace();
            this.getLogger().severe(e.getMessage());
        } finally {
            currentThread.setContextClassLoader(previousClassLoader);
        }
    }

    public List<String> onTabComplete(CommandSender sender, Command cmd,
                                      String alias,
                                      String[] args)
    {
        List<String> result = new ArrayList<String>();
        if (this.engine == null) {
            this.getLogger().severe(NO_JAVASCRIPT_MESSAGE);
            return null;
        }
        try {
            Invocable inv = (Invocable)this.engine;
            inv.invokeFunction("__onTabComplete", result, sender, cmd, alias, args);
        } catch (Exception e) {
            sender.sendMessage(e.getMessage());
            e.printStackTrace();
        }
        return result;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args)
    {
        boolean result = false;
        Object jsResult = null;
        if (this.engine == null) {
            this.getLogger().severe(NO_JAVASCRIPT_MESSAGE);
            return false;
        }
        try {
            jsResult = ((Invocable)this.engine).invokeFunction("__onCommand", sender, cmd, label, args);
        } catch (Exception se) {
            this.getLogger().severe(se.toString());
            se.printStackTrace();
            sender.sendMessage(se.getMessage());
        }
        if (jsResult != null){
            return ((Boolean)jsResult).booleanValue();
        }
        return result;
    }

    /** Class loader to search system classes first, then plugin classes. */
    protected class ContextClassLoader extends ClassLoader
    {
        public ContextClassLoader()
        {
            super(ClassLoader.getSystemClassLoader());
        }

        protected Class findClass(String name) throws ClassNotFoundException
        {
            return ScriptCraftPlugin.this.getClassLoader().loadClass(name);
        }

        protected URL findResource(String name)
        {
            return ScriptCraftPlugin.this.getClassLoader().getResource(name);
        }
    }
}
