package net.contrapt.jeditutil;

/**
* This class describes a dynamic property
*/
public class DynamicPropertyDescriptor {

   private enum Role {
      PROVIDER,
      CONSUMER
   };

   public final Class who;
   public final String name;
   public final String description;
   public final Role role;

   public DynamicPropertyDescriptor(final Class who, final String name, final String description, boolean isProvider) {
      this.who = who;
      this.name = name;
      this.description = description;
      this.role = ( isProvider ) ? Role.PROVIDER : Role.CONSUMER;
   }
}
