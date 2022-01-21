/*
 * Decompiled with CFR 0.0.9 (FabricMC cc05e23f).
 */
package website.skylorbeck.magehand.entity.goals;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;
import website.skylorbeck.magehand.entity.MageHandAbstractEntity;

public class MageHandPutItemInChestGoal
extends MoveToTargetPosGoal {
    MageHandAbstractEntity magehand;
    public MageHandPutItemInChestGoal(MageHandAbstractEntity mageHand, double speed, int range) {
        super(mageHand, speed, range,6);
        this.magehand = mageHand;
    }

    @Override
    public double getDesiredDistanceToTarget() {
        return 0;
    }

    @Override
    protected int getInterval(PathAwareEntity mob) {
        return 20;
    }

    @Override
    public boolean canStart() {
        return !magehand.getMainHandStack().isEmpty() && super.canStart();
    }

    @Override
    public boolean shouldContinue() {
        return !magehand.getMainHandStack().isEmpty() && super.shouldContinue();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    @Override
    public void tick() {
            ItemStack itemStack = magehand.getMainHandStack();
            BlockState blockState = this.magehand.world.getBlockState(this.magehand.getBlockPos().down());
            if (blockState.hasBlockEntity()) {
                BlockEntity blockEntity = this.magehand.world.getBlockEntity(magehand.getBlockPos().down());
                if (blockEntity instanceof SidedInventory sidedInventory) {
                    int[] is = sidedInventory.getAvailableSlots(Direction.DOWN);
                    for (int i = 0; i < is.length && !itemStack.isEmpty(); ++i) {
                        itemStack = transfer( sidedInventory, itemStack, is[i]);
                        magehand.equipStack(EquipmentSlot.MAINHAND,itemStack);
                    }
                } else if (blockEntity instanceof Inventory inventory) {
                    int sidedInventory = ((Inventory)blockEntity).size();
                    for (int is = 0; is < sidedInventory && !itemStack.isEmpty(); ++is) {
                        itemStack = transfer( inventory, itemStack, is);
                        magehand.equipStack(EquipmentSlot.MAINHAND,itemStack);
                    }
                }
            }
        super.tick();
    }
    private static ItemStack transfer(Inventory to, ItemStack stack, int slot) {
        ItemStack itemStack = to.getStack(slot);
        if (canInsert(to, stack, slot)) {
            int j;
            boolean bl = false;
            if (itemStack.isEmpty()) {
                to.setStack(slot, stack);
                stack = ItemStack.EMPTY;
                bl = true;
            } else if (canMergeItems(itemStack, stack)) {
                int i = stack.getMaxCount() - itemStack.getCount();
                j = Math.min(stack.getCount(), i);
                stack.decrement(j);
                itemStack.increment(j);
            }
            if (bl) {
                to.markDirty();
            }
        }
        return stack;
    }
    private static boolean canInsert(Inventory inventory, ItemStack stack, int slot) {
        if (!inventory.isValid(slot, stack)) {
            return false;
        }
        return !(inventory instanceof SidedInventory) || ((SidedInventory)inventory).canInsert(slot, stack, Direction.DOWN);
    }
    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        if (!first.isOf(second.getItem())) {
            return false;
        }
        if (first.getDamage() != second.getDamage()) {
            return false;
        }
        if (first.getCount() > first.getMaxCount()) {
            return false;
        }
        return ItemStack.areNbtEqual(first, second);
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        if (!world.isAir(pos.up()) || !pos.isWithinDistance(magehand.getStartingPos(),2f)) {
            return false;
        }
        BlockState blockState = world.getBlockState(pos);
        return blockState.hasBlockEntity() && (world.getBlockEntity(pos) instanceof Inventory);
    }
}

