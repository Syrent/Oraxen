package io.th0rgal.oraxen.recipes;

import io.th0rgal.oraxen.OraxenPlugin;
import io.th0rgal.oraxen.recipes.listeners.RecipesBuilderEvents;
import io.th0rgal.oraxen.recipes.listeners.RecipesEventsManager;
import io.th0rgal.oraxen.recipes.loaders.FurnaceLoader;
import io.th0rgal.oraxen.recipes.loaders.ShapedLoader;
import io.th0rgal.oraxen.recipes.loaders.ShapelessLoader;
import io.th0rgal.oraxen.settings.ConfigUpdater;
import io.th0rgal.oraxen.settings.MessageOld;
import io.th0rgal.oraxen.settings.Plugin;
import io.th0rgal.oraxen.settings.ResourcesManager;
import io.th0rgal.oraxen.utils.logs.Logs;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

public class RecipesManager {

    public static void load(JavaPlugin plugin) {
        if ((boolean) Plugin.RESET_RECIPES.getValue())
            Bukkit.resetRecipes();
        Bukkit.getPluginManager().registerEvents(new RecipesBuilderEvents(), plugin);
        File recipesFolder = new File(OraxenPlugin.get().getDataFolder(), "recipes");
        if (!recipesFolder.exists()) {
            recipesFolder.mkdirs();
            new ResourcesManager(plugin).extractConfigsInFolder("recipes", "yml");
        }
        registerAllConfigRecipesFromFolder(recipesFolder);
        RecipesEventsManager.get().registerEvents();
    }

    public static void reload(JavaPlugin plugin) {
        if ((boolean) Plugin.RESET_RECIPES.getValue())
            Bukkit.resetRecipes();
        RecipesEventsManager.get().resetRecipes();
        File recipesFolder = new File(OraxenPlugin.get().getDataFolder(), "recipes");
        if (!recipesFolder.exists()) {
            recipesFolder.mkdirs();
            new ResourcesManager(plugin).extractConfigsInFolder("recipes", "yml");
        }
        registerAllConfigRecipesFromFolder(recipesFolder);
        RecipesEventsManager.get().registerEvents();
    }

    private static void registerAllConfigRecipesFromFolder(File recipesFolder) {
        for (File configFile : Objects.requireNonNull(recipesFolder.listFiles()))
            registerConfigRecipes(configFile);
    }

    private static void registerConfigRecipes(File configFile) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
        boolean update = ConfigUpdater.update(configFile, config);
        for (String recipeSetting : config.getKeys(false)) {
            if (!config.isConfigurationSection(recipeSetting))
                continue;
            ConfigurationSection recipeSection = config.getConfigurationSection(recipeSetting);
            registerRecipeByType(configFile, recipeSection);
        }
        if (update)
            try {
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    private static void registerRecipeByType(File configFile, ConfigurationSection recipeSection) {
        try {
            switch (configFile.getName()) {
            case "shaped.yml":
                new ShapedLoader(recipeSection).registerRecipe();
                break;
            case "shapeless.yml":
                new ShapelessLoader(recipeSection).registerRecipe();
                break;
            case "furnace.yml":
                new FurnaceLoader(recipeSection).registerRecipe();
                break;
            default:
                Logs.logError(configFile.getName());
                break;
            }
        } catch (NullPointerException exception) {
            MessageOld.BAD_RECIPE.logError(recipeSection.getName());
        }
    }
}
