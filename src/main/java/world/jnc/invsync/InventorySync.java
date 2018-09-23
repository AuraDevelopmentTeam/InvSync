package world.jnc.invsync;

import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;
import lombok.NonNull;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.bstats.sponge.Metrics;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.SpongeEventFactory;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.EventContext;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import world.jnc.invsync.config.Config;
import world.jnc.invsync.event.PlayerEvents;
import world.jnc.invsync.permission.PermissionRegistry;
import world.jnc.invsync.util.database.DataSource;
import world.jnc.invsync.util.metrics.FeatureChart;

@Plugin(
  id = InventorySync.ID,
  name = InventorySync.NAME,
  version = InventorySync.VERSION,
  description = InventorySync.DESCRIPTION,
  url = InventorySync.URL,
  authors = {InventorySync.AUTHOR}
)
public class InventorySync {
  public static final String ID = "@id@";
  public static final String NAME = "@name@";
  public static final String VERSION = "@version@";
  public static final String DESCRIPTION = "@description@";
  public static final String URL = "https://github.com/AuraDevelopmentTeam/InvSync";
  public static final String AUTHOR = "The_BrainStone";

  @NonNull @Getter private static InventorySync instance = null;

  @Inject @NonNull private PluginContainer container;
  @Inject private Metrics metrics;
  @Inject @NonNull private Logger logger;

  @Inject private GuiceObjectMapperFactory factory;

  @Inject
  @DefaultConfig(sharedRoot = false)
  private ConfigurationLoader<CommentedConfigurationNode> loader;

  @Inject
  @ConfigDir(sharedRoot = false)
  @NonNull
  private Path configDir;

  @NonNull private Config config;

  @NonNull private DataSource dataSource;
  private PermissionRegistry permissionRegistry;
  private List<AutoCloseable> eventListeners = new LinkedList<>();

  public InventorySync() {
    if (instance != null) throw new IllegalStateException("Instance already exists!");

    instance = this;
  }

  public static PluginContainer getContainer() {
    return instance.container;
  }

  public static Logger getLogger() {
    return instance.logger;
  }

  public static Path getConfigDir() {
    return instance.configDir;
  }

  public static Config getConfig() {
    return instance.config;
  }

  public static DataSource getDataSource() {
    return instance.dataSource;
  }

  @Listener
  public void preInit(GamePreInitializationEvent event) throws IOException, ObjectMappingException {
    final TypeToken<Config> configToken = TypeToken.of(Config.class);

    logger.debug("Loading config...");

    CommentedConfigurationNode node =
        loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(factory));

    final Object globalValue = node.getNode("global").getValue();

    if (globalValue != null) {
      node.getNode("general").setValue(globalValue);
      node.removeChild("global");
    }

    // TODO: handle exception when invalid database type is specified
    config = node.<Config>getValue(configToken, Config::new);

    logger.debug("Saving config...");
    node.setValue(configToken, config);
    loader.save(node);
  }

  @Listener
  public void init(GameInitializationEvent event) throws SQLException {
    logger.info("Initializing " + NAME + " Version " + VERSION);

    if (VERSION.contains("SNAPSHOT")) {
      logger.warn("WARNING! This is a snapshot version!");
      logger.warn("Use at your own risk!");
    }
    if (VERSION.contains("development")) {
      logger.info("This is a unreleased development version!");
      logger.info("Things might not work properly!");
    }

    if (permissionRegistry == null) {
      permissionRegistry = new PermissionRegistry(this);
      logger.debug("Registered permissions");
    }

    dataSource = new DataSource();

    addEventListener(new PlayerEvents(dataSource, config.getSynchronize()));
    logger.debug("Registered events");

    logger.info("Loaded successfully!");
  }

  @Listener
  public void onServerStart(GameStartedServerEvent event) {
    metrics.addCustomChart(new FeatureChart("features"));
  }

  @Listener
  public void reload(GameReloadEvent event) throws Exception {
    Cause cause =
        Cause.builder()
            .append(this)
            .build(EventContext.builder().add(EventContextKeys.PLUGIN, container).build());

    // Unregistering everything
    GameStoppingEvent gameStoppingEvent = SpongeEventFactory.createGameStoppingEvent(cause);
    stop(gameStoppingEvent);

    // Starting over
    GamePreInitializationEvent gamePreInitializationEvent =
        SpongeEventFactory.createGamePreInitializationEvent(cause);
    preInit(gamePreInitializationEvent);
    GameInitializationEvent gameInitializationEvent =
        SpongeEventFactory.createGameInitializationEvent(cause);
    init(gameInitializationEvent);

    logger.info("Reloaded successfully!");
  }

  @Listener
  public void stop(GameStoppingEvent event) throws Exception {
    logger.info("Shutting down " + NAME + " Version " + VERSION);

    removeEventListeners();
    logger.debug("Unregistered events");

    dataSource = null;
    logger.debug("Closed database connection");

    config = null;
    logger.debug("Unloaded config");

    logger.info("Unloaded successfully!");
  }

  private void addEventListener(AutoCloseable listener) {
    eventListeners.add(listener);

    Sponge.getEventManager().registerListeners(this, listener);
  }

  private void removeEventListeners() throws Exception {
    for (AutoCloseable listener : eventListeners) {
      Sponge.getEventManager().unregisterListeners(listener);

      listener.close();
    }

    eventListeners.clear();
  }
}
