import { test, expect } from "@playwright/test";
import {
  loginOmaOpintopolku,
  logoutOmaOpintopolku,
  oppijaUrl,
} from "../fixtures/oma-opintopolku";
import { raimo } from "../fixtures/users";
import {
  loginVirkailijaOpintopolku,
  virkailijaUrl,
} from "../fixtures/virkailija-opintopolku";

test("kirjautuminen ja uloskirjautuminen ohjaavat oikeisiin osoitteisiin", async ({
  browser,
  page: oppijaPage,
}) => {
  const virkailijaContext = await browser.newContext();
  const virkailijaPage = await virkailijaContext.newPage();

  await oppijaPage.goto(oppijaUrl);
  await oppijaPage.getByRole("button", { name: "Hyväksy evästeet" }).click();
  await loginOmaOpintopolku(oppijaPage, raimo.hetu);

  await virkailijaPage.goto(virkailijaUrl);
  await loginVirkailijaOpintopolku(virkailijaPage);

  await logoutOmaOpintopolku(oppijaPage, raimo.name);

  await expect(oppijaPage).toHaveURL(
    "https://untuvaopintopolku.fi/oma-opintopolku/"
  );
  await expect(virkailijaPage).toHaveURL(
    "https://virkailija.untuvaopintopolku.fi/virkailijan-tyopoyta/"
  );

  await virkailijaPage
    .locator('[data-selenium-id="notification-categories-collapse-title"]')
    .click();

  await virkailijaContext.close();
});
