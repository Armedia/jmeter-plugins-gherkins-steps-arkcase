/*******************************************************************************
 * #%L
 * Armedia ArkCase
 * %%
 * Copyright (C) 2020 Armedia, LLC
 * %%
 * This file is part of the ArkCase software.
 *
 * If the software was purchased under a paid ArkCase license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * ArkCase is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ArkCase is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ArkCase. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 *******************************************************************************/
package com.arkcase.sim.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.ToIntBiFunction;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public class ByTools {

	public static class Pred {
		public static Predicate<WebElement> textMatches(Predicate<String> matcher) {
			Objects.requireNonNull(matcher, "Must provide a non-null predicate to apply");
			return (webElement) -> matcher.test(webElement.getText());
		}

		public static Predicate<WebElement> textEquals(String text) {
			return Pred.textEquals(text, false);
		}

		public static Predicate<WebElement> textEquals(String text, boolean ignoreCase) {
			if (text == null) { throw new IllegalArgumentException("Must provide a non-null string"); }
			final String expected = (StringUtils.isNotEmpty(text) ? text : StringUtils.EMPTY);
			final BiPredicate<String, String> equals = (ignoreCase ? StringUtils::equalsIgnoreCase
				: StringUtils::equals);
			return (webElement) -> equals.test(webElement.getText(), expected);
		}

		public static Predicate<WebElement> textStartsWith(String text) {
			return Pred.textStartsWith(text, false);
		}

		public static Predicate<WebElement> textStartsWith(String text, boolean ignoreCase) {
			if (StringUtils.isEmpty(text)) { throw new IllegalArgumentException("Must provide a non-empty string"); }
			final BiPredicate<String, String> startsWith = (ignoreCase ? StringUtils::startsWithIgnoreCase
				: StringUtils::startsWith);
			return (webElement) -> startsWith.test(webElement.getText(), text);
		}

		public static Predicate<WebElement> textEndsWith(String text) {
			return Pred.textEndsWith(text, false);
		}

		public static Predicate<WebElement> textEndsWith(String text, boolean ignoreCase) {
			if (StringUtils.isEmpty(text)) { throw new IllegalArgumentException("Must provide a non-empty string"); }
			final BiPredicate<String, String> endsWith = (ignoreCase ? StringUtils::endsWithIgnoreCase
				: StringUtils::endsWith);
			return (webElement) -> endsWith.test(webElement.getText(), text);
		}

		public static Predicate<WebElement> textContains(String text) {
			return Pred.textContains(text, false);
		}

		public static Predicate<WebElement> textContains(String text, boolean ignoreCase) {
			if (StringUtils.isEmpty(text)) { throw new IllegalArgumentException("Must provide a non-empty string"); }
			final ToIntBiFunction<String, String> indexOf = (ignoreCase ? StringUtils::indexOfIgnoreCase
				: StringUtils::indexOf);
			return (webElement) -> indexOf.applyAsInt(webElement.getText(), text) >= 0;
		}

		public static Predicate<WebElement> textMatches(String regEx) {
			Objects.requireNonNull(regEx, "Must provide a valid regular expression");
			final Pattern p = Pattern.compile(regEx);
			return (webElement) -> p.matcher(webElement.getText()).matches();
		}
	}

	public static final class ByWithPredicate extends By {
		private final Predicate<WebElement> predicate;
		private final By selector;

		private ByWithPredicate(By selector, Predicate<WebElement> predicate) {
			this.selector = selector;
			this.predicate = predicate;
		}

		public Predicate<WebElement> getPredicate() {
			return this.predicate;
		}

		public By getSelector() {
			return this.selector;
		}

		@Override
		public List<WebElement> findElements(SearchContext context) {
			List<WebElement> matches = new LinkedList<>();
			context.findElements(this.selector).stream() //
				.filter(this.predicate) //
				.forEach(matches::add) //
			;
			return matches;
		}
	}

	public static By cssMatching(String cssSelector, Predicate<WebElement> predicate) {
		return ByTools.addPredicate(By.cssSelector(cssSelector), predicate);
	}

	public static By cssContainingText(String cssSelector, String text) {
		return ByTools.cssMatching(cssSelector, Pred.textContains(text));
	}

	public static ByWithPredicate addPredicate(By selector, Predicate<WebElement> predicate) {
		Objects.requireNonNull(selector, "Must provide a By selector");
		Objects.requireNonNull(predicate, "Must provide a Predicate");

		if (ByWithPredicate.class.isInstance(selector)) {
			// This is to add efficiency and avoid multiple lookups
			ByWithPredicate bwp = ByWithPredicate.class.cast(selector);
			selector = bwp.selector;
			predicate = bwp.predicate.and(predicate);
		}

		return new ByWithPredicate(selector, predicate);
	}

	public static By matching(Predicate<WebElement> predicate) {
		return ByTools.addPredicate(By.cssSelector("*"), predicate);
	}

	public static By textMatches(Predicate<String> matcher) {
		return ByTools.matching(ByTools.Pred.textMatches(matcher));
	}

	public static By textContains(final String text) {
		return ByTools.matching(Pred.textContains(text));
	}

	public static By textContains(final String text, boolean ignoreCase) {
		return ByTools.matching(Pred.textContains(text, ignoreCase));
	}

	public static By textEquals(final String text) {
		return ByTools.matching(Pred.textEquals(text));
	}

	public static By textEquals(final String text, boolean ignoreCase) {
		return ByTools.matching(Pred.textEquals(text, ignoreCase));
	}

	public static By textStartsWith(final String text) {
		return ByTools.matching(Pred.textStartsWith(text));
	}

	public static By textStartsWith(final String text, boolean ignoreCase) {
		return ByTools.matching(Pred.textStartsWith(text, ignoreCase));
	}

	public static By textEndsWith(final String text) {
		return ByTools.matching(Pred.textEndsWith(text));
	}

	public static By textEndsWith(final String text, boolean ignoreCase) {
		return ByTools.matching(Pred.textEndsWith(text, ignoreCase));
	}

	public static By textMatches(final String regEx) {
		return ByTools.matching(Pred.textMatches(regEx));
	}

	private static final String[] NG_PREFIXES = {
		"ng-", "ng_", "data-ng-", "x-ng-", "ng\\:"
	};

	private static final String NG_MODEL_CSS_TEMPLATE = "[${0}model=\"${1}\"]";

	public static By ngModel(final String model) {
		if (StringUtils.isEmpty(model)) {
			throw new IllegalArgumentException("Must provide a non-empty model name to search for");
		}
		return new By() {
			@Override
			public List<WebElement> findElements(SearchContext context) {
				final String template = TextTools.interpolate(ByTools.NG_MODEL_CSS_TEMPLATE, null, model);
				for (String prefix : ByTools.NG_PREFIXES) {
					By by = By.cssSelector(TextTools.interpolate(template, prefix));
					List<WebElement> matches = by.findElements(context);
					if (!matches.isEmpty()) { return matches; }
				}
				return Collections.emptyList();
			}
		};
	}

	/**
	 * <p>
	 * Will succeed when at least one element is found for each of the search conditions given.
	 * </p>
	 */
	public static By byOneEach(By... locators) {
		Objects.requireNonNull(locators, "Must provide a non-null array of By instances");
		final List<By> finalLocators = Arrays.asList(locators);
		finalLocators.removeIf(Objects::isNull);
		if (finalLocators.isEmpty()) {
			throw new IllegalArgumentException("Must provide at least one non-null By instance");
		}
		return new By() {
			@Override
			public List<WebElement> findElements(SearchContext context) {
				List<WebElement> ret = null;
				int matches = 0;
				for (By by : finalLocators) {
					List<WebElement> l = by.findElements(context);
					if (!l.isEmpty()) {
						if (ret == null) {
							ret = new ArrayList<>();
						}
						ret.addAll(l);
						matches++;
					}
				}
				return (matches == finalLocators.size() ? ret : Collections.emptyList());
			}
		};
	}

	/**
	 * <p>
	 * Will succeed when any of the given locators returns at least one element.
	 * </p>
	 */
	public static By firstOf(By... locators) {
		Objects.requireNonNull(locators, "Must provide a non-null array of By instances");
		final List<By> finalLocators = Arrays.asList(locators);
		finalLocators.removeIf(Objects::isNull);
		if (finalLocators.isEmpty()) {
			throw new IllegalArgumentException("Must provide at least one non-null By instance");
		}
		return new By() {
			@Override
			public List<WebElement> findElements(SearchContext context) {
				for (By by : finalLocators) {
					List<WebElement> l = by.findElements(context);
					if (!l.isEmpty()) { return l; }
				}
				return Collections.emptyList();
			}
		};
	}
}