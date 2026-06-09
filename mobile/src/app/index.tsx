import { Redirect } from 'expo-router';
import { useAuth } from '@/contexts/AuthContext';

export default function IndexScreen() {
  const { isLoading, token } = useAuth();

  if (isLoading) {
    return null;
  }

  return token ? <Redirect href="/home" /> : <Redirect href="/login" />;
}
