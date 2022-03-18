package net.tarcadia.tribina.erod.stylename.util;

import org.jetbrains.annotations.NotNull;

public enum Style {

    Normal{
        @Override
        @NotNull
        public String styled(@NotNull String name) {
            return "§r" + name + "§r";
        }
    },

    GreenName{
        @Override
        @NotNull
        public String styled(@NotNull String name) {
            return "§a" + name + "§r";
        }
    },

    YellowName{
        @Override
        @NotNull
        public String styled(@NotNull String name) {
            return "§e" + name + "§r";
        }
    },

    BlueName{
        @Override
        @NotNull
        public String styled(@NotNull String name) {
            return "§9" + name + "§r";
        }
    },

    GoldName{
        @Override
        @NotNull
        public String styled(@NotNull String name) {
            return "§6" + name + "§r";
        }
    },

    CyanName{
        @Override
        @NotNull
        public String styled(@NotNull String name) {
            return "§b" + name + "§r";
        }
    },

    PinkName{
        @Override
        @NotNull
        public String styled(@NotNull String name) {
            return "§d" + name + "§r";
        }
    },

    HeavyCyanName{
        @Override
        @NotNull
        public String styled(@NotNull String name) {
            return "§b§o§l" + name + "§r";
        }
    },

    HeavyPinkName{
        @Override
        @NotNull
        public String styled(@NotNull String name) {
            return "§d§o§l" + name + "§r";
        }
    },

    ;

    @NotNull
    public abstract String styled(@NotNull String name);

}
