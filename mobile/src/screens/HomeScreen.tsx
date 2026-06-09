import React, { useState, useEffect } from 'react';
import { View, Text, TouchableOpacity, StyleSheet, ScrollView, ActivityIndicator } from 'react-native';
import { useAuth } from '../contexts/AuthContext';
import { useSubscription } from '../contexts/SubscriptionContext';
import { MovePlayedEvent } from '../types';

interface DisplayedEvent {
  id: string;
  timestamp: string;
  receivedAt: number;
}

export const HomeScreen: React.FC = () => {
  const { user, logout } = useAuth();
  const { isConnected, isReconnecting, subscribe, unsubscribe } = useSubscription();
  const [events, setEvents] = useState<DisplayedEvent[]>([]);
  const subscriptionRef = React.useRef<string | null>(null);

  // Subscribe to move.played events
  useEffect(() => {
    const handleMovePlayedEvent = (event: MovePlayedEvent) => {
      if (event.type === 'move.played') {
        const displayedEvent: DisplayedEvent = {
          id: event.data.id,
          timestamp: event.data.timestamp,
          receivedAt: Date.now(),
        };

        setEvents((prevEvents) => {
          // Keep only the last 10 events
          const updated = [displayedEvent, ...prevEvents];
          return updated.slice(0, 10);
        });
      }
    };

    // Subscribe to move.played events
    subscriptionRef.current = subscribe({ eventType: 'move.played' }, handleMovePlayedEvent);

    return () => {
      if (subscriptionRef.current) {
        unsubscribe(subscriptionRef.current);
        subscriptionRef.current = null;
      }
    };
  }, [subscribe, unsubscribe]);

  const handleLogout = async () => {
    try {
      // Clean up subscription before logging out
      if (subscriptionRef.current) {
        unsubscribe(subscriptionRef.current);
        subscriptionRef.current = null;
      }
      await logout();
    } catch (error) {
      console.error('Logout error:', error);
    }
  };

  const getConnectionStatusColor = () => {
    if (isConnected) return '#34C759';
    if (isReconnecting) return '#FF9500';
    return '#FF3B30';
  };

  const getConnectionStatusText = () => {
    if (isConnected) return 'Connected';
    if (isReconnecting) return 'Reconnecting...';
    return 'Disconnected';
  };

  return (
    <View style={styles.container}>
      <ScrollView
        style={styles.scrollView}
        contentContainerStyle={styles.scrollViewContent}
        showsVerticalScrollIndicator={true}
      >
        <View style={styles.contentContainer}>
          <Text style={styles.welcomeText}>Welcome to App Go</Text>

          {user && (
            <>
              <Text style={styles.usernameText}>Logged in as:</Text>
              <Text style={styles.username}>{user.username}</Text>
            </>
          )}

          {/* Connection Status */}
          <View style={styles.statusContainer}>
            <View
              style={[
                styles.statusIndicator,
                { backgroundColor: getConnectionStatusColor() },
              ]}
            />
            <Text style={styles.statusText}>{getConnectionStatusText()}</Text>
            {isReconnecting && <ActivityIndicator size="small" color="#FF9500" style={styles.spinner} />}
          </View>

          <Text style={styles.infoText}>Real-time Event Updates</Text>

          {/* Events Display */}
          <View style={styles.eventsContainer}>
            {events.length === 0 ? (
              <Text style={styles.noEventsText}>
                Waiting for move.played events...
              </Text>
            ) : (
              <>
                <Text style={styles.eventsHeaderText}>Recent Events ({events.length})</Text>
                {events.map((event, index) => (
                  <View key={`${event.id}-${index}`} style={styles.eventItem}>
                    <Text style={styles.eventId}>ID: {event.id}</Text>
                    <Text style={styles.eventTime}>
                      {new Date(event.timestamp).toLocaleTimeString()}
                    </Text>
                  </View>
                ))}
              </>
            )}
          </View>

          <TouchableOpacity style={styles.logoutButton} onPress={handleLogout}>
            <Text style={styles.logoutButtonText}>Logout</Text>
          </TouchableOpacity>
        </View>
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  scrollView: {
    flex: 1,
  },
  scrollViewContent: {
    flexGrow: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 16,
  },
  contentContainer: {
    width: '100%',
    maxWidth: 500,
    backgroundColor: '#fff',
    borderRadius: 8,
    padding: 20,
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
    elevation: 3,
  },
  welcomeText: {
    fontSize: 28,
    fontWeight: 'bold',
    marginBottom: 20,
    color: '#333',
  },
  usernameText: {
    fontSize: 14,
    color: '#666',
    marginBottom: 8,
  },
  username: {
    fontSize: 18,
    fontWeight: '600',
    color: '#007AFF',
    marginBottom: 20,
  },
  statusContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 20,
    paddingHorizontal: 12,
    paddingVertical: 8,
    backgroundColor: '#f9f9f9',
    borderRadius: 6,
  },
  statusIndicator: {
    width: 10,
    height: 10,
    borderRadius: 5,
    marginRight: 8,
  },
  statusText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#333',
  },
  spinner: {
    marginLeft: 8,
  },
  infoText: {
    fontSize: 14,
    color: '#999',
    marginBottom: 16,
    textAlign: 'center',
  },
  eventsContainer: {
    width: '100%',
    maxHeight: 300,
    marginBottom: 24,
    paddingHorizontal: 12,
    paddingVertical: 12,
    backgroundColor: '#f0f0f0',
    borderRadius: 6,
    borderColor: '#e0e0e0',
    borderWidth: 1,
  },
  eventsHeaderText: {
    fontSize: 12,
    fontWeight: '600',
    color: '#666',
    marginBottom: 8,
  },
  eventItem: {
    paddingHorizontal: 8,
    paddingVertical: 6,
    marginBottom: 6,
    backgroundColor: '#fff',
    borderRadius: 4,
    borderLeftColor: '#007AFF',
    borderLeftWidth: 3,
  },
  eventId: {
    fontSize: 12,
    color: '#333',
    fontWeight: '500',
  },
  eventTime: {
    fontSize: 11,
    color: '#999',
    marginTop: 4,
  },
  noEventsText: {
    fontSize: 13,
    color: '#999',
    textAlign: 'center',
    fontStyle: 'italic',
  },
  logoutButton: {
    backgroundColor: '#FF3B30',
    borderRadius: 4,
    padding: 12,
    paddingHorizontal: 24,
    alignItems: 'center',
  },
  logoutButtonText: {
    color: '#fff',
    fontSize: 16,
    fontWeight: '600',
  },
});
