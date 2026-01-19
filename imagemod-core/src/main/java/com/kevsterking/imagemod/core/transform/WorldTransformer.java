package com.kevsterking.imagemod.core.transform;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;

import java.util.Stack;
import java.util.function.Function;

public class WorldTransformer {

  private final Stack<WorldTransform> undo_stack = new Stack<>();
  private final Stack<WorldTransform> redo_stack = new Stack<>();
  private final IntegratedServer server;

  public WorldTransformer() {
    this.server = Minecraft.getInstance().getSingleplayerServer();
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
    IntegratedServer server = Minecraft.getInstance().getSingleplayerServer();
    if (server == null) {
      callback.apply(new Exception("Failed to perform undo: server not found"));
      return;
    }
    server.execute(() -> {
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
    WorldStructure structure,
    WorldTransformCreation creation,
    Function<Void, Void> callback
  ) throws Exception {
    if (this.server == null) {
      throw new Exception("Multiplayer not supported yet...");
    }
    server.execute(() -> {
      WorldTransform ret = new WorldTransform(server.getLevel(creation.level.dimension()), structure, creation);
      ret.perform_transform();
      this.undo_stack.push(ret);
      callback.apply(null);
    });
  }

}
