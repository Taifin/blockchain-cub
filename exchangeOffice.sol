pragma solidity ^0.8.7;

contract CurrencyExchange {

    // Declare state variables of the contract
    address public owner;
    mapping (address => uint) public balances;
    mapping (address => bool) public authorizedTraders;
    uint public exchangeRate = 100; // 1 ETH = 100 USD

    // When 'CurrencyExchange' contract is deployed:
    // 1. set the deploying address as the owner of the contract
    // 2. set the deployed smart contract's ETH balance to 0
    constructor() {
        owner = msg.sender;
        balances[address(this)] = 0;
    }

    // Allow the owner to add or remove authorized traders
    function addAuthorizedTrader(address _trader) public {
        require(msg.sender == owner, "Only the owner can add authorized traders.");
        authorizedTraders[_trader] = true;
    }

    function removeAuthorizedTrader(address _trader) public {
        require(msg.sender == owner, "Only the owner can remove authorized traders.");
        authorizedTraders[_trader] = false;
    }

    // Allow the owner to deposit ETH into the smart contract
    function deposit() public payable {
        require(msg.sender == owner, "Only the owner can deposit ETH into the contract.");
        balances[address(this)] += msg.value;
    }

    // Allow authorized traders to exchange their ETH for USD
    function sell(uint _amount) public {
        require(authorizedTraders[msg.sender] == true, "Only authorized traders can sell ETH for USD.");
        require(balances[address(this)] >= _amount * exchangeRate, "Not enough USD in stock to complete this exchange.");
        balances[msg.sender] += _amount * exchangeRate;
        balances[address(this)] -= _amount * exchangeRate;
    }

    // Allow anyone to purchase ETH with USD
    function buy(uint _amount) public payable {
        require(msg.value == _amount / exchangeRate, "You must pay the exact amount in USD for the ETH.");
        require(balances[msg.sender] >= _amount, "Not enough USD in your balance to complete this purchase.");
        balances[msg.sender] -= _amount;
        balances[address(this)] += _amount / exchangeRate;
        payable(msg.sender).transfer(msg.value);
    }
}
