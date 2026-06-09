import Constants from 'expo-constants';

const ENV = {
  dev: {
    API_URL: 'http://localhost:8080',
    ENVIRONMENT: 'dev',
  },
  prod: {
    API_URL: 'https://api.appgo.com',
    ENVIRONMENT: 'prod',
  },
};

const getEnvVars = () => {
  if (__DEV__) {
    return ENV.dev;
  }
  return ENV.prod;
};

export const Config = getEnvVars();
