describe('Authentification', () => {
  it('connecte l’utilisateur et redirige vers /etudiants', () => {
    cy.intercept('POST', '/api/login', 'fake-jwt').as('login');
    cy.intercept('GET', '/api/etudiants', []).as('list');

    cy.visit('/login');
    cy.getDataCy('login-input').type('john');
    cy.getDataCy('password-input').type('password');
    cy.getDataCy('login-submit').click();

    cy.wait('@login');
    cy.url().should('include', '/etudiants');
  });

  it('affiche une erreur si les identifiants sont invalides', () => {
    cy.intercept('POST', '/api/login', { statusCode: 400, body: 'Invalid credentials' }).as('login');

    cy.visit('/login');
    cy.getDataCy('login-input').type('bad');
    cy.getDataCy('password-input').type('bad');
    cy.getDataCy('login-submit').click();

    cy.wait('@login');
    cy.getDataCy('login-error').should('be.visible');
  });

  it('protège la page étudiants (redirige vers /login sans token)', () => {
    cy.visit('/etudiants');
    cy.url().should('include', '/login');
  });
});
