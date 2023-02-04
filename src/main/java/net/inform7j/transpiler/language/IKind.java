package net.inform7j.transpiler.language;

import java.util.Optional;

import net.inform7j.transpiler.tokenizer.TokenString;

public interface IKind extends IStory.Element {
	TokenString name();
	Optional<? extends IKind> superKind();
	default boolean canAssignTo(IKind target) {
		if(name().equals(target.name())) return true;
		return superKind().map(sup -> sup.canAssignTo(target)).orElse(false);
	}
	Optional<? extends IProperty> getProperty(TokenString prop);
}
