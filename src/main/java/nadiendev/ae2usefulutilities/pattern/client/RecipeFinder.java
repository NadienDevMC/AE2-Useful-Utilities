package nadiendev.ae2usefulutilities.pattern.client;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

public interface RecipeFinder {

    @Nullable
    Component getWorkstationName(String categoryId);

    @Nullable
    static RecipeFinder get() {
        return Holder.INSTANCE;
    }

    final class Holder {
        @Nullable
        static final RecipeFinder INSTANCE = null;

        private Holder() {
        }
    }
}
