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
package com.arkcase.sim.gherkin.steps.components;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.jbehave.core.annotations.Aliases;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import com.arkcase.sim.components.html.TextBoxHelper;
import com.arkcase.sim.tools.ExpectedConditionTools;
import com.armedia.commons.jmeter.gherkin.GherkinContext;

public class LoginPageSteps extends ComponentSteps {

	private static final By SHARECARE_LOGIN_BUTTON = By.cssSelector("form#loginForm span#submitButton");
	private static final By SHARECARE_USERNAME = By.cssSelector("form#loginForm input#userNameInput");
	private static final By SHARECARE_PASSWORD = By.cssSelector("form#loginForm input#passwordInput");

	private static final By ARMEDIA_LOGIN_BUTTON = By
		.cssSelector("form#MainForm input#ContentPlaceHolder1_SubmitButton");
	private static final By ARMEDIA_USERNAME = By
		.cssSelector("form#MainForm input#ContentPlaceHolder1_UsernameTextBox");
	private static final By ARMEDIA_PASSWORD = By
		.cssSelector("form#MainForm input#ContentPlaceHolder1_PasswordTextBox");

	@When("the sign in page is ready")
	@Aliases(values = { //
		"the signin page is ready", //
		"the authentication page is ready", //
		"the log in page is ready", //
		"the login page is ready", //
	})
	public void waitForLogin() {
		getWaitHelper().waitUntil( //
			ExpectedConditionTools.firstOf( //
				ExpectedConditions.elementToBeClickable(LoginPageSteps.ARMEDIA_LOGIN_BUTTON), //
				ExpectedConditions.elementToBeClickable(LoginPageSteps.SHARECARE_LOGIN_BUTTON) //
			) //
		);
	}

	@Then("sign in")
	@Aliases(values = { //
		"signin", //
		"authenticate", //
		"log in", //
		"login" //
	})
	public void login() {
		login(null);
	}

	@Then("sign in as [$user]/[$pass]")
	@Aliases(values = { //
		"signin as [$user]/[$pass]", //
		"authenticate as [$user]/[$pass]", //
		"log in as [$user]/[$pass]", //
		"login as [$user]/[$pass]" //
	})
	public void login(@Named("user") String user, @Named("pass") String pass) {
		login(Pair.of(user, pass));
	}

	private void login(Pair<String, String> creds) {

		// Get the username
		GherkinContext ctx = GherkinContext.get();

		final String userName;
		final String password;

		if (creds != null) {
			userName = (creds.getKey() != null ? creds.getKey() : StringUtils.EMPTY);
			password = (creds.getValue() != null ? creds.getValue() : StringUtils.EMPTY);
		} else {
			userName = ctx.getVars().get("userName");
			password = ctx.getVars().get("password");
		}

		// Here we cheat a little so we don't expose the authentication credentials
		TextBoxHelper helper = new TextBoxHelper(getBrowser());

		// First things first: which page are we on?
		WebElement button = helper.waitUntil( //
			ExpectedConditionTools.firstOf( //
				ExpectedConditions.elementToBeClickable(LoginPageSteps.ARMEDIA_LOGIN_BUTTON), //
				ExpectedConditions.elementToBeClickable(LoginPageSteps.SHARECARE_LOGIN_BUTTON) //
			) //
		);

		final By userNameField;
		final By passwordField;
		if (StringUtils.equals("submitButton", button.getAttribute("id"))) {
			// Sharecare page
			userNameField = LoginPageSteps.SHARECARE_USERNAME;
			passwordField = LoginPageSteps.SHARECARE_PASSWORD;
		} else {
			// Armedia page
			userNameField = LoginPageSteps.ARMEDIA_USERNAME;
			passwordField = LoginPageSteps.ARMEDIA_PASSWORD;
		}

		if (StringUtils.isNotEmpty(userName)) {
			helper.sendKeys(userNameField, userName);
		}
		if (StringUtils.isNotEmpty(password)) {
			helper.sendKeys(passwordField, password);
		}
		button.click();
	}
}