package net.tarcadia.tribina.erod.stylename.util;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public enum Skin {

    Default{
        @Override
        public @NotNull String skinValue(@NotNull Player player) {
            return Alex.skinValue(player);
        }

        @Override
        public @NotNull String skinSignature(@NotNull Player player) {
            return Alex.skinSignature(player);
        }
    },

    Own{

        @Override
        synchronized public @NotNull String skinValue(@NotNull Player player) {
            return Objects.requireNonNullElse(SkinLoader.getTextureValue(player), Alex.skinValue(player));
        }

        @Override
        synchronized public @NotNull String skinSignature(@NotNull Player player) {
            return Objects.requireNonNullElse(SkinLoader.getTextureSignature(player), Alex.skinValue(player));
        }
    },

    Steve{
        @Override
        public @NotNull String skinValue(@NotNull Player player) {
            return "ewogICJ0aW1lc3RhbXAiIDogMTY0NzQ0NjQ4ODcxNiwKICAicHJvZmlsZUlkIiA6ICIzNjNjMjA3MTllMDI0NGFhOTcxM2FkY2I3NzU0MzgzYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUYXJjYWRpYSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xYTRhZjcxODQ1NWQ0YWFiNTI4ZTdhNjFmODZmYTI1ZTZhMzY5ZDE3NjhkY2IxM2Y3ZGYzMTlhNzEzZWI4MTBiIgogICAgfQogIH0KfQ==";
        }

        @Override
        public @NotNull String skinSignature(@NotNull Player player) {
            return "qu/RNr/G1NilGW0jz45dZBAclfzELQOrRJup7gCdq0UA2aryQSRa7j0a0mm34neQkeTLaV+O/4T3Rt7aqYKVQMt72VPc+DClBZVhRQTaFfxOBI2ZwXYfvmMj80J0WsgRaXmwIuUwaYiekdpnC4TEp9PX0REvidxXI6vc+CYjSlfh6/EvW/EXGWH/hImImbFx2NJYtHAFpAwkjt32y+m+NHKVxcogm3N+pgan+fV19jrCWpnWsmIR9Ai3qpSpKPKkc5tKoFJvIDOwtqqd9dBZEGyxBlvbVqU1CcHMPqlbUtmXgFInO5wHOTb9C6j5g6ALo57Y6YVqar3s4Rv5x9ma5JJ15xE9q4fhC4RF8yxsQ8eN8C5a5/K+fmRufPJX6Peh6iaI+s7KjkzGxwxc6htuH4qzKlJpxWayE/V05HocZO9jJTXBZZZEOPgPsB0GDxCkzg5VH2uqsXv543vbnulMm6pKfQoFt4LtHx+KtOjF8jx/vsPlTDL+LzbXs09WtU5JQprImAHx0GAfQpvb+tLbtnZ/gEDcPCiSLON2kGcH0yhNPi4IxG1Be9ZRa/Nr6ROJEzdu3nW0YgO4nQGKSFDWrhPSZd+G63DuIKzJrNmDNMeCE8N7r5FQyodt6bI7H96d06A41RikYUUQUU2wI0hT3suPQWx5RfqavTzdQWzwjg0=";
        }
    },

    Alex{
        @Override
        public @NotNull String skinValue(@NotNull Player player) {
            return "ewogICJ0aW1lc3RhbXAiIDogMTY0NzQ0NjMwNTExMiwKICAicHJvZmlsZUlkIiA6ICIzNjNjMjA3MTllMDI0NGFhOTcxM2FkY2I3NzU0MzgzYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUYXJjYWRpYSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zYjYwYTFmNmQ1NjJmNTJhYWViYmYxNDM0ZjFkZTE0NzkzM2EzYWZmZTBlNzY0ZmE0OWVhMDU3NTM2NjIzY2QzIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
        }

        @Override
        public @NotNull String skinSignature(@NotNull Player player) {
            return "E4YkM4T8b5MSFSbqJ27gvBtvLmXpuGf+LRyqWaftkyl0qhisNnZi7NFOou9NyRrWMSIFkT+L/tOqvPW9VG2bjPnu/r6vfNoY00scRkRhfqXXhOS3FjHj/08sqahJ+lyHiC59BWDQwWnKKt376uifiAiRX7yC+QeDz4YbPICwWR9YMoUR4ZRC4NQ0q3wFJX1hDC7ZaoPqSshq7SBXFN9K7Hirkr0rJzuKGZCL/lhuZas3nY5H3diiArTbtuRXrW84Q3dO0AAnrSJqGNqwIM2Gw1OtlEsNM96bxmOmOcbLkzBy9iEWPRGWw+mG007mjthyqbLVT/DnZXafE1zg3/rEZr8l9ToTm4GVsfJ5nG0elEFJgJkJnTJsPfQiCIPKTSX0z9TH5j/2j95GYDjcgiSgjsKK1Vg6qLFLq87Xyq1AzOdvs4vFwKCqUUQMjCiZCrpghQDNgDRMgfU/vqP+6k9yUtS74kBobQ1FkYvf7sovgc58BGOE9P89JfksnUSzyMoA9LIfk70OPV8qHO83JXgMGPv5chrL8vYp7jNOecfkag/wMe9TJyEVS7e4r1GoTZYQptJgdxVVedHl+mvzD0K05LWTCQ7ptayARqzIaarMxwSMZ5YxddJQ2QU9bWiZXAkASi/2ppO5HaLfHsNHYp4gz83s88FQQPcUuH6gp/s3sUw=";
        }
    },

    Notch{
        @Override
        public @NotNull String skinValue(@NotNull Player player) {
            return "ewogICJ0aW1lc3RhbXAiIDogMTY0NzQyODY3ODAyMSwKICAicHJvZmlsZUlkIiA6ICIwNjlhNzlmNDQ0ZTk0NzI2YTViZWZjYTkwZTM4YWFmNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb3RjaCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yOTIwMDlhNDkyNWI1OGYwMmM3N2RhZGMzZWNlZjA3ZWE0Yzc0NzJmNjRlMGZkYzMyY2U1NTIyNDg5MzYyNjgwIgogICAgfQogIH0KfQ==";
        }

        @Override
        public @NotNull String skinSignature(@NotNull Player player) {
            return "DPdVdwxw3CR7/+ZlguZXqMP6PDGeODei9LI8hZTAWi4yyYSUJWBXdRuENlJRsk2EGq0miRkBIPyZVmoTRHcGzeVtVdZ/Bp3y3JFc2o9lvGjDCyG3DBpqi6cJbdNT/Ey7Cc2cvHcy3EG5nX+5Jg4rVNvJUgn5lT59HNHLkR0c0Pkb0V56xiv1Lo4gqMd6qB0eN7OEDqmb5O23g0BMgPJa3X09jVayovMExRd+x8bWXmlajE2hPOhXsOHLybKlzbhpZZy1gWeoEcXDhhCkeGxvsmmcKXJye8ZNdOVkCCqxURZlhxv3ZcSA56ShvYbotDh4pJafR5QZ8/glL2FUBmZEMYjy2ejl90d6uyXe4dJyWnWb54rQF7U1LY+DAhJ5I7Xapo18Ui94X+vSpZHi/vUu8gnj/PTZ2JfXsNvizr03e3AS0Acso/iiaVm5kCPSHoXbSZdN+HMEBs0/DTrR0ayzM3p9BU4wvOss8q2cZ9YIJkjxzN6BGFC5/F0PJ/+qSbTTTlX5UFae58uUTIA2/SY+jq7vUQjRTS9O4L29V2zXOSlUt3KlloK6luBJdnWUF/WJ8L1SzQuNso8f8BCDDOMHm2sTXD3uepEw4jEtUyfrH5SJXBws5NqUMJLkFflEQvypwltHJt2u4E3Iy0F4fdN1sk2eCnOv4bhUWI0eA3a3ayI=";
        }
    }

    ;

    @NotNull
    public abstract String skinValue(@NotNull Player player);

    @NotNull
    public abstract String skinSignature(@NotNull Player player);

}
