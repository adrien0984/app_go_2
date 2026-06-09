import 'package:http/http.dart' as http;
import 'dart:convert';
import '../models/game_state.dart';

class GameService {
  final String baseUrl;
  final String Function() getToken;

  GameService({
    required this.baseUrl,
    required this.getToken,
  });

  Future<String> createGame({int boardSize = 19}) async {
    final response = await http.post(
      Uri.parse('$baseUrl/games'),
      headers: {
        'Authorization': 'Bearer ${getToken()}',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'boardSize': boardSize}),
    );

    if (response.statusCode == 201) {
      final json = jsonDecode(response.body);
      return json['gameId'];
    } else {
      throw Exception('Failed to create game');
    }
  }

  Future<GameState> getGame(String gameId) async {
    final response = await http.get(
      Uri.parse('$baseUrl/games/$gameId'),
      headers: {
        'Authorization': 'Bearer ${getToken()}',
      },
    );

    if (response.statusCode == 200) {
      return GameState.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to fetch game');
    }
  }

  Future<GameState> playMove(String gameId, int row, int col) async {
    final response = await http.post(
      Uri.parse('$baseUrl/games/$gameId/moves'),
      headers: {
        'Authorization': 'Bearer ${getToken()}',
        'Content-Type': 'application/json',
      },
      body: jsonEncode({'row': row, 'col': col}),
    );

    if (response.statusCode == 200) {
      return GameState.fromJson(jsonDecode(response.body));
    } else if (response.statusCode == 422 || response.statusCode == 400) {
      final error = jsonDecode(response.body);
      throw Exception(error['message'] ?? 'Illegal move');
    } else {
      throw Exception('Failed to play move');
    }
  }

  Future<GameState> passMove(String gameId) async {
    final response = await http.post(
      Uri.parse('$baseUrl/games/$gameId/pass'),
      headers: {
        'Authorization': 'Bearer ${getToken()}',
      },
    );

    if (response.statusCode == 200) {
      return GameState.fromJson(jsonDecode(response.body));
    } else {
      throw Exception('Failed to pass move');
    }
  }
}
