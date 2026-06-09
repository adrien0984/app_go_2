import 'package:flutter/material.dart';

class AuthService extends ChangeNotifier {
  String? _accessToken;
  String? _refreshToken;

  String? get accessToken => _accessToken;
  bool get isAuthenticated => _accessToken != null;

  void setTokens(String accessToken, String refreshToken) {
    _accessToken = accessToken;
    _refreshToken = refreshToken;
    notifyListeners();
  }

  void logout() {
    _accessToken = null;
    _refreshToken = null;
    notifyListeners();
  }
}
