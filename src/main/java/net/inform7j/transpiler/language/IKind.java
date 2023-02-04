package net.inform7j.transpiler.language;

import java.util.Optional;

import net.inform7j.transpiler.tokenizer.TokenString;

public interface IKind extends IStory.Element {
	public TokenString name();
	public Optional<? extends IKind> superKind();
	public default boolean canAssignTo(IKind target) {
		if(name().equals(target.name())) return true;
		return superKind().map(sup -> sup.canAssignTo(target)).orElse(false);
	}
	public Optional<? extends IProperty> getProperty(TokenString prop);
}
