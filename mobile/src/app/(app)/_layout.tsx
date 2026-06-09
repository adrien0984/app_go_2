import { Stack } from 'expo-router';

export default function AppLayout() {
  return (
    <Stack
      screenOptions={{
        headerShown: true,
        animationEnabled: true,
      }}
    >
      <Stack.Screen
        name="home"
        options={{
          title: 'App Go',
          headerBackVisible: false,
        }}
      />
    </Stack>
  );
}
