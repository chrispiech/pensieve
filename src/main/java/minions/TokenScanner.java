package minions;

import java.util.*;

public class TokenScanner {
	private String buffer = "";
	boolean ignoreWhitespaceFlag = false;
	boolean ignoreCommentsFlag = true;
	boolean scanNumbersFlag = false;
	boolean scanStringsFlag = false;

	int cp;
	int length;

	private Set<String> operators;
	private Set<Character> wordChars;
	private List<String> savedTokens;
	private List<Character> savedCharacters;

	boolean inputSet = false;

	public TokenScanner(String karelCode) {
		setInput(karelCode);
	}

	public void setInput(String input) {
		inputSet = true;
		this.buffer = input;
		this.length = input.length();
		this.cp = 0;
		this.savedCharacters = new ArrayList<Character>();
		this.savedTokens = new ArrayList<String>();
		this.wordChars = new HashSet<Character>();
		this.operators = new HashSet<String>();
	}

	public boolean hasMoreTokens() {
		String token = this.nextToken();
		this.saveToken(token);
		return !token.isEmpty();
	}
	
	public String nextToken() {
		if (this.savedTokens.size() != 0) {
			return popSavedToken();
		}
		while (true) {
			if (this.ignoreWhitespaceFlag) this.skipSpaces();
			Character ch = this.getChar();
			if (ch == null) return "";
			if (ch == '/' && this.ignoreCommentsFlag) {
				ch = this.getChar();
				if (ch == '/') {
					while (true) {
						ch = this.getChar();
						if (ch == null || ch == '\n' || ch == '\r') {
							if(ch != null && ch == '\n') {
								this.saveChar(ch);
							}
							break;
						}
					}
					continue;
				} else if (ch == '*') {
					Character prev = null;
					while (true) {
						ch = this.getChar();
						if (ch == null || (prev != null && prev == '*' && ch == '/')) break;
						prev = ch;
					}
					continue;
				}
				this.saveChar(ch);
				ch = '/';
			}
			if ((ch == '"' || ch == '\'') && this.scanStringsFlag) {
				this.saveChar(ch);
				return this.scanString();
			}
			if (ch != null && Character.isDigit(ch) && this.scanNumbersFlag) {
				this.saveChar(ch);
				return this.scanNumber();
			}
			if (this.isWordCharacter(ch)) {
				this.saveChar(ch);
				return this.scanWord();
			}
			String op = "" + ch;
			while (this.isOperatorPrefix(op)) {
				ch = this.getChar();
				if (ch == null) break;
				op += ch;
			}
			while (op.length() > 1 && !this.isOperator(op)) {
				this.saveChar(op.charAt(op.length() - 1));
				op = op.substring(0, op.length() - 1);
			}
			return op;
		}
	}
	
	public String peekNext() {
		String next = nextToken();
		saveToken(next);
		return next;
	}
	
	public void saveToken(String token) {
		savedTokens.add(token);
	}

	private String scanString() {
		String token = "";
		char delim = this.getChar();
		token += delim;
		while (true) {
			Character ch = this.getChar();
			if (ch == null) throw new Error("Unterminated string");
			if (ch == delim) break;
			if (ch == '\\') {
				token += this.scanEscapeCharacter();
			} else {
				token += ch;
			}
		}
		return token + delim;
	}

	private String scanEscapeCharacter() {
		String str = "\\";
		Character ch = this.getChar();
		str += ch;
		if (Character.isDigit(ch) || ch == 'x' || ch == 'u') {
			boolean hex = !Character.isDigit(ch);
			while (true) {
				ch = this.getChar();
				if ((hex) ? !isxdigit(ch) : !Character.isDigit(ch)) break;
				str += ch;
			}
			this.saveChar(ch);
		}
		return str;
	}


	private boolean isxdigit(Character ch) {
		if(ch == null) return false;
		System.out.println("Warning, I don't parse hex digits");
		return Character.isDigit(ch);
	}

	private boolean isOperator(String op) {
		return operators.contains(op);
	}

	private String scanWord() {
		String token = "";
		while (true) {
			Character ch = this.getChar();
			if (ch == null) break;
			if (!this.isWordCharacter(ch)) {
				this.saveChar(ch);
				break;
			}
			token += ch;
		}
		return token;
	}

	private static final int INITIAL_STATE = 0;
	private static final int BEFORE_DECIMAL_POINT = 1;
	private static final int AFTER_DECIMAL_POINT = 2;
	private static final int STARTING_EXPONENT = 3;
	private static final int FOUND_EXPONENT_SIGN = 4;
	private static final int SCANNING_EXPONENT = 5;
	private static final int LEADING_ZERO = 6;
	private static final int SCANNING_HEX = 7;
	private static final int FINAL_STATE = 8;

	private String scanNumber() {
		String token = "";
		int state = TokenScanner.INITIAL_STATE;
		while (state != TokenScanner.FINAL_STATE) {
			Character ch = this.getChar();
			char xch = 'e';
			switch (state) {
			case TokenScanner.INITIAL_STATE:
				if (ch == '0') {
					state = TokenScanner.LEADING_ZERO;
				} else {
					state = TokenScanner.BEFORE_DECIMAL_POINT;
				}
				break;
			case TokenScanner.BEFORE_DECIMAL_POINT:
				if (ch == '.') {
					state = TokenScanner.AFTER_DECIMAL_POINT;
				} else if (ch == 'E' || ch == 'e') {
					state = TokenScanner.STARTING_EXPONENT;
					xch = ch;
				} else if (!Character.isDigit(ch)) {
					this.saveChar(ch);
					state = TokenScanner.FINAL_STATE;
				}
				break;
			case TokenScanner.AFTER_DECIMAL_POINT:
				if (ch == 'E' || ch == 'e') {
					state = TokenScanner.STARTING_EXPONENT;
					xch = ch;
				} else if (!Character.isDigit(ch)) {
					this.saveChar(ch);
					state = TokenScanner.FINAL_STATE;
				}
				break;
			case TokenScanner.STARTING_EXPONENT:
				if (ch == '+' || ch == '-') {
					state = TokenScanner.FOUND_EXPONENT_SIGN;
				} else if (Character.isDigit(ch)) {
					state = TokenScanner.SCANNING_EXPONENT;
				} else {
					this.saveChar(ch);
					state = TokenScanner.FINAL_STATE;
				}
				break;
			case TokenScanner.FOUND_EXPONENT_SIGN:
				if (Character.isDigit(ch)) {
					state = TokenScanner.SCANNING_EXPONENT;
				} else {
					this.saveChar(ch);
					this.saveChar(xch);
					state = TokenScanner.FINAL_STATE;
				}
				break;
			case TokenScanner.SCANNING_EXPONENT:
				if (!Character.isDigit(ch)) {
					this.saveChar(ch);
					state = TokenScanner.FINAL_STATE;
				}
				break;
			case TokenScanner.LEADING_ZERO:
				if (ch == 'x' || ch == 'X') {
					state = TokenScanner.SCANNING_HEX;
				} else if (ch == '.') {
					state = TokenScanner.AFTER_DECIMAL_POINT;
				} else if (ch == 'E' || ch == 'e') {
					state = TokenScanner.STARTING_EXPONENT;
					xch = ch;
				} else if (!Character.isDigit(ch)) {
					this.saveChar(ch);
					state = TokenScanner.FINAL_STATE;
				}
				break;
			case TokenScanner.SCANNING_HEX:
				if (!isxdigit(ch)) {
					this.saveChar(ch);
					state = TokenScanner.FINAL_STATE;
				}
				break;
			default:
				state = TokenScanner.FINAL_STATE;
				break;
			}
			if (state != TokenScanner.FINAL_STATE) {
				token += ch;
			}
		}
		return token;
	}

	private boolean isOperatorPrefix(String op) {
		for (String str : this.operators) {
			if (str.startsWith(op)) return true;
		}
		return false;
	}

	private boolean isWordCharacter(Character ch) {
		if(ch == null) return false;
		return Character.isAlphabetic(ch) || Character.isDigit(ch) || this.wordChars.contains(ch);
	}

	private void saveChar(Character ch) {
		this.cp--;
		savedCharacters.add(ch);
	}

	private void skipSpaces() {
		while (true) {
			Character ch = this.getChar();
			if (ch == null) return;
			if (!Character.isWhitespace(ch)) {
				this.saveChar(ch);
				return;
			}
		}
	}

	private Character getChar() {
		if (this.savedCharacters.size() == 0) {
			return (this.cp >= this.length) ? null : this.buffer.charAt(this.cp++);
		} else {
			this.cp++;
			return popSavedCharacter();
		}
	}

	private Character popSavedCharacter() {
		return this.savedCharacters.remove(savedCharacters.size() -1 );
	}

	private String popSavedToken() {
		return this.savedTokens.remove(savedTokens.size() -1 );
	}

	
}
