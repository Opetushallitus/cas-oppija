import { Page } from "@playwright/test";

export const virkailijaUrl = "https://virkailija.untuvaopintopolku.fi/";

export const loginVirkailijaOpintopolku = async (page: Page) => {
  await page.locator('img[src="/cas/images/suomi.fi-logo.png"]').click();
  await page.getByRole('link', { name: 'Test IdP' }).click();
  await page.getByRole('link', { name: 'Käytä oletusta 210281-9988' }).click();
  await page.getByRole('button', { name: 'Tunnistaudu' }).click();
  await page.getByRole('button', { name: 'Continue to service' }).click();
}
