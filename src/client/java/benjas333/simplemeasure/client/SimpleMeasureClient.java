package benjas333.simplemeasure.client;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;

public class SimpleMeasureClient implements ClientModInitializer {
    private static final ArrayList<Vec3> positions = new ArrayList<>();

    private static final Color red = new Color(255, 0, 0);
    private static final Color yellow = new Color(255, 255, 0);

    @Override
    public void onInitializeClient() {
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> dispatcher.register(
                        ClientCommandManager
                                .literal("measure")
                                .then(
                                        ClientCommandManager
                                                .literal("start")
                                                .executes(this::startCommand)
                                )
                                .then(
                                        ClientCommandManager
                                                .literal("end")
                                                .executes(this::endCommand)
                                )
                                .then(
                                        ClientCommandManager
                                                .literal("add")
                                                .executes(this::addCommand)
                                )
                )
        );
    }

    private void addPos(@NotNull CommandContext<FabricClientCommandSource> context) {
        var pos = context.getSource().getPosition();
        positions.add(pos);
        context
                .getSource()
                .sendFeedback(Component.translatable(
                        "text.simplemeasure.pos.added",
                        positions.size(),
                        Utils.formatNumber(pos.x),
                        Utils.formatNumber(pos.y),
                        Utils.formatNumber(pos.z)
                ));
    }

    private MutableComponent getMeasurementSummary() {
        if (positions.size() < 2) {
            return Component
                    .translatable("text.simplemeasure.measure.invalid")
                    .withColor(red.getRGB());
        }

        if (positions.size() == 2) {
            var pos1 = positions.getFirst();
            var pos2 = positions.getLast();

            var difference = pos2.subtract(pos1);
            var distance = difference.length();
            var exactVolume = Utils.getVectorVolume(difference);

            var blockArea = new BlockArea(pos1, pos2);
            var blockVolume = blockArea.getVolume();

            return Component.translatable(
                    "text.simplemeasure.measure.simple",
                    Utils.formatNumber(distance),
                    Utils.formatNumber(exactVolume),
                    Utils.formatNumber(Math.abs(difference.x)),
                    Utils.formatNumber(Math.abs(difference.y)),
                    Utils.formatNumber(Math.abs(difference.z)),
                    Utils.formatNumber(blockVolume),
                    Utils.formatNumber(blockArea.x),
                    Utils.formatNumber(blockArea.y),
                    Utils.formatNumber(blockArea.z)
            );
        }

        var distanceSummary = Component.translatable("text.simplemeasure.measure.multiple.header.distance");
        var exactVolumeSummary = Component.translatable("text.simplemeasure.measure.multiple.header.volume.exact");
        var blockVolumeSummary = Component.translatable("text.simplemeasure.measure.multiple.header.volume.blocks");
        double distancesAddition = 0, exactVolumesAddition = 0, blockVolumesAddition = 0;

        Vec3 lastPos, currentPos = positions.getFirst();
        for (int i = 1; i < positions.size(); i++) {
            lastPos = currentPos;
            currentPos = positions.get(i);

            var difference = currentPos.subtract(lastPos);
            var distance = difference.length();
            var exactVolume = Utils.getVectorVolume(difference);

            var blockArea = new BlockArea(lastPos, currentPos);
            var blockVolume = blockArea.getVolume();

            distancesAddition += distance;
            exactVolumesAddition += exactVolume;
            blockVolumesAddition += blockVolume;

            distanceSummary.append(Component.translatable(
                    "text.simplemeasure.measure.multiple.field.distance",
                    i,
                    i + 1,
                    Utils.formatNumber(distance)
            ));
            exactVolumeSummary.append(Component.translatable(
                    "text.simplemeasure.measure.multiple.field.volume.exact",
                    i,
                    i + 1,
                    Utils.formatNumber(exactVolume),
                    Utils.formatNumber(Math.abs(difference.x)),
                    Utils.formatNumber(Math.abs(difference.y)),
                    Utils.formatNumber(Math.abs(difference.z))
            ));
            blockVolumeSummary.append(Component.translatable(
                    "text.simplemeasure.measure.multiple.field.volume.blocks",
                    i,
                    i + 1,
                    Utils.formatNumber(blockVolume),
                    Utils.formatNumber(blockArea.x),
                    Utils.formatNumber(blockArea.y),
                    Utils.formatNumber(blockArea.z)
            ));
        }

        distanceSummary.append(Component.translatable(
                "text.simplemeasure.measure.multiple.field.distance.addition",
                Utils.formatNumber(distancesAddition)
        ));
        exactVolumeSummary.append(Component.translatable(
                "text.simplemeasure.measure.multiple.field.volume.exact.addition",
                Utils.formatNumber(exactVolumesAddition)
        ));
        blockVolumeSummary.append(Component.translatable(
                "text.simplemeasure.measure.multiple.field.volume.blocks.addition",
                Utils.formatNumber(blockVolumesAddition)
        ));

        var first = positions.getFirst();
        var last = positions.getLast();

        var difference = last.subtract(first);
        var distance = difference.length();
        var volume = Utils.getVectorVolume(difference);

        var blockArea = new BlockArea(first, last);
        var blockVolume = blockArea.getVolume();

        distanceSummary.append(Component.translatable(
                "text.simplemeasure.measure.multiple.field.distance",
                1,
                positions.size(),
                Utils.formatNumber(distance)
        ));
        exactVolumeSummary.append(Component.translatable(
                "text.simplemeasure.measure.multiple.field.volume.exact",
                1,
                positions.size(),
                Utils.formatNumber(volume),
                Utils.formatNumber(Math.abs(difference.x)),
                Utils.formatNumber(Math.abs(difference.y)),
                Utils.formatNumber(Math.abs(difference.z))
        ));
        blockVolumeSummary.append(Component.translatable(
                "text.simplemeasure.measure.multiple.field.volume.blocks",
                1,
                positions.size(),
                Utils.formatNumber(blockVolume),
                Utils.formatNumber(blockArea.x),
                Utils.formatNumber(blockArea.y),
                Utils.formatNumber(blockArea.z)
        ));

        return distanceSummary.append(exactVolumeSummary).append(blockVolumeSummary);
    }

    private int startCommand(CommandContext<FabricClientCommandSource> context) {
        if (!positions.isEmpty()) {
            positions.clear();
            context
                    .getSource()
                    .sendFeedback(
                            Component
                                    .translatable("command.simplemeasure.feedback.start.warning")
                                    .withColor(yellow.getRGB())
                    );
        }

        addPos(context);
        return 1;
    }

    private int endCommand(CommandContext<FabricClientCommandSource> context) {
        var response = addCommand(context);
        if (response != 1) return response;

        context
                .getSource()
                .sendFeedback(getMeasurementSummary());
        positions.clear();
        return 1;
    }

    private int addCommand(CommandContext<FabricClientCommandSource> context) {
        if (positions.isEmpty()) {
            context
                    .getSource()
                    .sendFeedback(
                            Component
                                    .translatable("command.simplemeasure.feedback.add.error")
                                    .withColor(red.getRGB())
                    );
            return 0;
        }

        addPos(context);
        return 1;
    }
}