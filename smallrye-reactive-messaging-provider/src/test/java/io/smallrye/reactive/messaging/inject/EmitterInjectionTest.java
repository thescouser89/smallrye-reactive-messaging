package io.smallrye.reactive.messaging.inject;

import io.smallrye.reactive.messaging.WeldTestBaseWithoutTails;
import io.smallrye.reactive.messaging.annotations.Emitter;
import io.smallrye.reactive.messaging.annotations.Stream;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Message;
import org.junit.Test;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.DeploymentException;
import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.assertj.core.api.Assertions.assertThat;

public class EmitterInjectionTest extends WeldTestBaseWithoutTails {

  @Test
  public void testWithPayloads() {
    addBeanClass(SourceBean.class);
    MyBeanEmittingPayloads bean = installInitializeAndGet(MyBeanEmittingPayloads.class);
    bean.run();
    assertThat(bean.emitter()).isNotNull();
    assertThat(bean.list()).containsExactly("a", "b", "c");
  }

  @Test
  public void testWithMessages() {
    addBeanClass(SourceBean.class);
    MyBeanEmittingMessages bean = installInitializeAndGet(MyBeanEmittingMessages.class);
    bean.run();
    assertThat(bean.emitter()).isNotNull();
    assertThat(bean.list()).containsExactly("a", "b", "c");
  }

  @Test
  public void testWithNull() {
    addBeanClass(SourceBean.class);
    MyBeanEmittingNull bean = installInitializeAndGet(MyBeanEmittingNull.class);
    bean.run();
    assertThat(bean.emitter()).isNotNull();
    assertThat(bean.list()).containsExactly("a", "b", "c");
    assertThat(bean.isCaught()).isTrue();
  }

  @Test(expected = DeploymentException.class)
  public void testWithMissingStream() {
    addBeanClass(SourceBean.class);
    installInitializeAndGet(BeanWithMissingStream.class);
  }

  @ApplicationScoped
  public static class MyBeanEmittingPayloads {
    @Inject @Stream("foo") Emitter<String> emitter;
    private List<String> list = new CopyOnWriteArrayList<>();

    public Emitter<String> emitter() {
      return emitter;
    }

    public List<String> list() {
      return list;
    }

    public void run() {
      emitter.send("a").send("b").send("c").complete();
    }

    @Incoming("foo")
    public void consume(String s) {
      list.add(s);
    }
  }

  @ApplicationScoped
  public static class MyBeanEmittingMessages {
    @Inject @Stream("foo") Emitter<Message<String>> emitter;
    private List<String> list = new CopyOnWriteArrayList<>();

    public Emitter<Message<String>> emitter() {
      return emitter;
    }

    public List<String> list() {
      return list;
    }

    public void run() {
      emitter.send(Message.of("a"));
      emitter.send(Message.of("b"));
      emitter.send(Message.of("c"));
      emitter.complete();
    }

    @Incoming("foo")
    public void consume(String s) {
      list.add(s);
    }
  }

  public static class BeanWithMissingStream {
    @Inject @Stream("missing") Emitter<Message<String>> emitter;
    private List<String> list = new CopyOnWriteArrayList<>();

    public Emitter<Message<String>> emitter() {
      return emitter;
    }
  }

  @ApplicationScoped
  public static class MyBeanEmittingNull {
    @Inject @Stream("foo") Emitter<String> emitter;
    private List<String> list = new CopyOnWriteArrayList<>();
    private boolean caught;

    public Emitter<String> emitter() {
      return emitter;
    }

    public boolean isCaught() {
      return true;
    }

    public List<String> list() {
      return list;
    }

    public void run() {
      emitter.send("a").send("b");
      try {
        emitter.send(null);
      } catch (IllegalArgumentException e) {
        caught = true;
      }
      emitter.send("c").complete();
    }

    @Incoming("foo")
    public void consume(String s) {
      list.add(s);
    }
  }

}