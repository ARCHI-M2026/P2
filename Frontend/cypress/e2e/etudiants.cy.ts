describe('Gestion des étudiants', () => {
  beforeEach(() => {
    cy.loginByToken();
  });

  it('affiche la liste des étudiants', () => {
    cy.intercept('GET', '/api/students', [
      { id: 1, firstName: 'Ada', lastName: 'Lovelace', email: 'ada@ex.com', classe: 'L3' }
    ]).as('list');

    cy.visit('/etudiants');
    cy.wait('@list');
    cy.getDataCy('etu-row').should('have.length', 1);
    cy.contains('Ada');
  });

  it('ajoute un étudiant', () => {
    cy.intercept('GET', '/api/students', []).as('list');
    cy.intercept('POST', '/api/students', {
      id: 2, firstName: 'Alan', lastName: 'Turing', email: 'alan@ex.com', classe: 'M1'
    }).as('create');

    cy.visit('/etudiants');
    cy.wait('@list');
    cy.getDataCy('etu-firstName').type('Alan');
    cy.getDataCy('etu-lastName').type('Turing');
    cy.getDataCy('etu-email').type('alan@ex.com');
    cy.getDataCy('etu-classe').type('M1');
    cy.getDataCy('etu-submit').click();
    cy.wait('@create').its('request.body').should('include', { email: 'alan@ex.com' });
  });

  it('supprime un étudiant', () => {
    cy.intercept('GET', '/api/students', [
      { id: 1, firstName: 'Ada', lastName: 'Lovelace', email: 'ada@ex.com', classe: 'L3' }
    ]).as('list');
    cy.intercept('DELETE', '/api/students/1', { statusCode: 204 }).as('del');

    cy.visit('/etudiants');
    cy.wait('@list');
    cy.getDataCy('etu-delete').first().click();
    cy.wait('@del');
  });
});
