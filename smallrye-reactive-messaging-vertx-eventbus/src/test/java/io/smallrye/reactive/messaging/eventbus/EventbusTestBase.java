package io.smallrye.reactive.messaging.eventbus;

import java.io.File;

import org.eclipse.microprofile.config.ConfigProvider;
import org.jboss.weld.environment.se.Weld;
import org.junit.After;
import org.junit.Before;

import io.smallrye.config.SmallRyeConfigProviderResolver;
import io.smallrye.reactive.messaging.MediatorFactory;
import io.smallrye.reactive.messaging.extension.MediatorManager;
import io.smallrye.reactive.messaging.extension.ReactiveMessagingExtension;
import io.smallrye.reactive.messaging.impl.ConfiguredChannelFactory;
import io.smallrye.reactive.messaging.impl.InternalChannelRegistry;
import io.vertx.reactivex.core.Vertx;

public class EventbusTestBase {

    protected EventBusUsage usage;
    Vertx vertx;

    static Weld baseWeld() {
        Weld weld = new Weld();
        weld.addBeanClass(MediatorFactory.class);
        weld.addBeanClass(MediatorManager.class);
        weld.addBeanClass(InternalChannelRegistry.class);
        weld.addBeanClass(ConfiguredChannelFactory.class);
        weld.addExtension(new ReactiveMessagingExtension());
        weld.addBeanClass(VertxEventBusConnector.class);

        weld.addExtension(new io.smallrye.config.inject.ConfigExtension());

        weld.disableDiscovery();
        return weld;
    }

    public static void addConfig(MapBasedConfig config) {
        if (config != null) {
            config.write();
        } else {
            clear();
        }
    }

    public static void clear() {
        File out = new File("target/test-classes/META-INF/microprofile-config.properties");
        if (out.delete()) {
            out.delete();
        }
    }

    @Before
    public void setup() {
        vertx = Vertx.vertx();
        usage = new EventBusUsage(vertx.eventBus().getDelegate());
    }

    @After
    public void tearDown() {
        vertx.close();
        SmallRyeConfigProviderResolver.instance().releaseConfig(ConfigProvider.getConfig());
    }

}
