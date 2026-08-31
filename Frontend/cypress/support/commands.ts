/// <reference types="cypress" />
// ***********************************************
// This example commands.ts shows you how to
// create various custom commands and overwrite
// existing commands.
//
// For more comprehensive examples of custom
// commands please read more here:
// https://on.cypress.io/custom-commands
// ***********************************************
//
//
// -- This is a parent command --
// Cypress.Commands.add('login', (email, password) => { ... })
//
//
// -- This is a child command --
// Cypress.Commands.add('drag', { prevSubject: 'element'}, (subject, options) => { ... })
//
//
// -- This is a dual command --
// Cypress.Commands.add('dismiss', { prevSubject: 'optional'}, (subject, options) => { ... })
//
//
// -- This will overwrite an existing command --
// Cypress.Commands.overwrite('visit', (originalFn, url, options) => { ... })
//
// declare global {
//   namespace Cypress {
//     interface Chainable {
//       login(email: string, password: string): Chainable<void>
//       drag(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       dismiss(subject: string, options?: Partial<TypeOptions>): Chainable<Element>
//       visit(originalFn: CommandOriginalFn, url: string, options: Partial<VisitOptions>): Chainable<Element>
//     }
//   }
// }

// Simule un utilisateur authentifié en injectant un JWT dans le localStorage.
Cypress.Commands.add('loginByToken', (token: string = 'fake-jwt') => {
  window.localStorage.setItem('auth_token', token);
});

declare global {
  namespace Cypress {
    interface Chainable {
      loginByToken(token?: string): Chainable<void>;
    }
  }
}
export {};

Cypress.Commands.add("getDataCy", (selector, ...args) => {
    if(/\s/.test(selector)){
        let sels = selector.split(" ")

        return cy.get(`[data-cy=${sels[0]}] ${sels[1]}`, ...args).then(element => {
            return element
        })
    }else{
        return cy.get(`[data-cy=${selector}]`, ...args).then(element => {
            return element
        })
    }
})