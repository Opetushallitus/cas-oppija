import { Page } from "@playwright/test";
import { defaultUser } from "./users";

export const oppijaUrl = "https://untuvaopintopolku.fi/oma-opintopolku/";

export const loginOmaOpintopolku = async (page: Page, hetu?: string) => {
  await page.getByRole("button", { name: "Kirjaudu sisään" }).click();
  await page.getByRole("link", { name: "Testitunnistaja" }).click();
  if (hetu) {
    await page.type("#hetu_input", hetu);
  } else {
    await page
      .getByRole("link", { name: "Käytä oletusta 210281-9988" })
      .click();
  }
  await page.getByRole("button", { name: "Tunnistaudu" }).click();
  await page.getByRole("button", { name: "Jatka palveluun" }).click();
};

export const logoutOmaOpintopolku = async (page: Page, username?: string) => {
  await page.getByRole("button", { name: username ?? defaultUser.name }).click();
  await page.getByRole("button", { name: "Kirjaudu ulos" }).click();
};
