package ch.njol.skript.expressions;

import ch.njol.skript.bukkitutil.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.Event;
import org.bukkit.event.block.SignChangeEvent;
import org.jetbrains.annotations.Nullable;

import ch.njol.skript.Skript;
import ch.njol.skript.aliases.Aliases;
import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.classes.Changer.ChangeMode;
import ch.njol.skript.doc.Description;
import ch.njol.skript.doc.Examples;
import ch.njol.skript.doc.Name;
import ch.njol.skript.doc.Since;
import ch.njol.skript.effects.Delay;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.skript.lang.util.SimpleLiteral;
import ch.njol.util.Kleenean;

/**
 * @author Peter Güttinger
 */
@Name("Sign Text")
@Description("A line of text on a sign. Can be changed, but remember that there is a 16 character limit per line (including color codes that use 2 characters each).")
@Examples({"on rightclick on sign:",
		"	line 2 of the clicked block is \"[Heal]\":",
		"		heal the player",
		"	set line 3 to \"%player%\""})
@Since("1.3")
public class ExprSignText extends SimpleExpression<String> {
	static {
		Skript.registerExpression(ExprSignText.class, String.class, ExpressionType.PROPERTY,
				"[the] line %number% [of %block%]", "[the] (1¦1st|1¦first|2¦2nd|2¦second|3¦3rd|3¦third|4¦4th|4¦fourth) line [of %block%]");
	}
	
	@SuppressWarnings("null")
	private Expression<Number> line;
	@SuppressWarnings("null")
	private Expression<Block> block;
	
	@SuppressWarnings({"unchecked", "null"})
	@Override
	public boolean init(final Expression<?>[] exprs, final int matchedPattern, final Kleenean isDelayed, final ParseResult parseResult) {
		if (matchedPattern == 0)
			line = (Expression<Number>) exprs[0];
		else
			line = new SimpleLiteral<>(parseResult.mark, false);
		block = (Expression<Block>) exprs[exprs.length - 1];
		return true;
	}
	
	@Override
	public boolean isSingle() {
		return true;
	}
	
	@Override
	public Class<? extends String> getReturnType() {
		return String.class;
	}
	
	@Override
	@Nullable
	protected String[] get(final Event e) {
		final Number l = line.getSingle(e);
		if (l == null)
			return new String[0];
		final int line = l.intValue() - 1;
		if (line < 0 || line > 3)
			return new String[0];
		if (getTime() >= 0 && block.isDefault() && e instanceof SignChangeEvent && !Delay.isDelayed(e)) {
			return new String[] {((SignChangeEvent) e).getLine(line)};
		}
		final Block b = block.getSingle(e);
		if (b == null)
			return new String[0];
		if (!(b.getState() instanceof Sign))
			return new String[0];
		return new String[] {((Sign) b.getState()).getLine(line)};
	}
	
	@Override
	public String toString(final @Nullable Event e, final boolean debug) {
		return "line " + line.toString(e, debug) + " of " + block.toString(e, debug);
	}
	
	// TODO allow add, remove, and remove all (see ExprLore)
	@Override
	@Nullable
	public Class<?>[] acceptChange(final ChangeMode mode) {
		if (mode == ChangeMode.DELETE || mode == ChangeMode.SET)
			return new Class[] {String.class};
		return null;
	}
	
	static boolean hasUpdateBooleanBoolean = true;
	
	@SuppressWarnings("incomplete-switch")
	@Override
	public void change(final Event e, final @Nullable Object[] delta, final ChangeMode mode) throws UnsupportedOperationException {
		final Number l = line.getSingle(e);
		if (l == null)
			return;
		final int line = l.intValue() - 1;
		if (line < 0 || line > 3)
			return;
		final Block b = block.getSingle(e);
		if (b == null)
			return;
		if (getTime() >= 0 && e instanceof SignChangeEvent && b.equals(((SignChangeEvent) e).getBlock()) && !Delay.isDelayed(e)) {
			switch (mode) {
				case DELETE:
					((SignChangeEvent) e).setLine(line, "");
					break;
				case SET:
					assert delta != null;
					((SignChangeEvent) e).setLine(line, (String) delta[0]);
					break;
			}
		} else {
			BlockState state = b.getState();
			if (!(state instanceof Sign))
				return;
			Sign sign = (Sign) b.getState();
			switch (mode) {
				case DELETE:
					sign.setLine(line, "");
					break;
				case SET:
					assert delta != null;
					sign.setLine(line, (String) delta[0]);
					break;
			}
			if (hasUpdateBooleanBoolean) {
				try {
					sign.update(false, false);
				} catch (final NoSuchMethodError err) {
					hasUpdateBooleanBoolean = false;
					sign.update();
				}
			} else {
				sign.update();
			}
		}
	}
	
	@Override
	public boolean setTime(final int time) {
		return super.setTime(time, SignChangeEvent.class, block);
	}
	
}
