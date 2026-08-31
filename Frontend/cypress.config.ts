import { defineConfig } from 'cypress';

import coverageTask from '@cypress/code-coverage/task'

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:4200',
    setupNodeEvents(on, config) {
      // implement node event listeners here

      // Pour les anciennes versions
      // registerCodeCoverageTasks(on, config)
      // return config    

      coverageTask(on, config)
      return config 
    },
    specPattern: 'cypress/e2e/**/*.cy.ts',
    supportFile: 'cypress/support/e2e.ts',
    video: false
  },
  env: {
    codeCoverage: {
      exclude: ['cypress/**/*.*'],
    },
  }
});