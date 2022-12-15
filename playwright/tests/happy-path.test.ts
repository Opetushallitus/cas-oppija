import { test, expect } from "@playwright/test";
import {
  loginOmaOpintopolku,
  logoutOmaOpintopolku,
  oppijaUrl,
} from "../fixtures/oma-opintopolku";

test("oppijan kirjautuminen toimii", async ({ page }) => {
  await page.goto(oppijaUrl);
  await page.getByRole("button", { name: "Hyväksy evästeet" }).click();

  await loginOmaOpintopolku(page);
  await expect(page).toHaveURL(oppijaUrl);

  await logoutOmaOpintopolku(page);

  await page.getByRole("button", { name: "Nordea Demo" }).click();
  await page.getByRole("button", { name: "Kirjaudu ulos" }).click();

  await expect(page).toHaveURL(oppijaUrl);
  await page.getByRole("button", { name: "Kirjaudu sisään" }).click();
});
