package com.kevsterking.imagemod.fabric.WorldTransformer;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.Stack;
import java.util.function.Function;

public class WorldTransformer {

  private final Stack<WorldTransform> undo_stack = new Stack<>();
  private final Stack<WorldTransform> redo_stack = new Stack<>();
  private final IntegratedServer server;

  public WorldTransformer() {
    this.server = MinecraftClient.getInstance().getServer();
  }

  public void undo() throws Exception {
		if (this.undo_stack.empty()) throw new Exception("Undo stack is empty.");
		WorldTransform action = this.undo_stack.pop();;
    action.revert_transform();
    this.redo_stack.push(action);
  }

	public void redo() throws Exception {
		if (this.redo_stack.empty()) throw new Exception("Redo stack is empty.");
    WorldTransform action = this.redo_stack.pop();;
    action.perform_transform();
    this.undo_stack.push(action);
  }

  public void undo_async(Function<Exception,Void> callback) {
    this.server.execute(() -> {
      try {
        this.undo();
        callback.apply(null);
      } catch (Exception e) {
        callback.apply(e);
      }
    });
  }

  public void redo_async(Function<Exception,Void> callback) {
    this.server.execute(() -> {
      try {
        this.redo();
        callback.apply(null);
      } catch (Exception e) {
        callback.apply(e);
      }
    });
  }

  public void place_async(
		ClientWorld world,
    WorldStructure structure,
		BlockPos position,
		Direction direction_x,
		Direction direction_y,
		Direction direction_z,
		Function<Void, Void> callback
	) throws Exception {
    if (this.server == null) throw new Exception("Multiplayer support comming soon...");
    WorldTransform ret = new WorldTransform(
      this.server.getWorld(world.getRegistryKey()),
      position,
      direction_x,
      direction_y,
      direction_z,
      structure
    );
    this.server.execute(() -> {
      ret.perform_transform();
      this.undo_stack.push(ret);
      callback.apply(null);
    });
  }
}
